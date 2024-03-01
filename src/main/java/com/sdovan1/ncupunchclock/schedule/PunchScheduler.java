package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.bot.Publisher;
import com.sdovan1.ncupunchclock.ifttt.IftttPunchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class PunchScheduler {
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private BytesEncryptor encryptor;

    private final Map<Task, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    @Autowired
    private PunchRepository repository;
    @Autowired
    private Publisher publisher;
    @Autowired
    private PunchAgentFactory punchAgentFactory;

    public PunchScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }


    public void cancelScheduledPunch(Punch punch, TaskType taskType) {
        var task = new Task(punch, taskType);
        var scheduledFuture = taskMap.get(task);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            taskMap.remove(task);
            log.info("Canceled {} {} at {}", punch, taskType, taskType == TaskType.CLOCK_IN ? punch.getClockInTime() : punch.getClockOutTime());
        }
    }

    public void cancelScheduledPunch(Punch punch) {
        cancelScheduledPunch(punch, TaskType.CLOCK_IN);
        cancelScheduledPunch(punch, TaskType.CLOCK_OUT);
    }

    public void cancelAllScheduledPunches(List<Punch> punches) {
        punches.forEach(this::cancelScheduledPunch);
    }

    public void schedule(Punch punch, TaskType taskType) {
        var task = new Task(punch, taskType);
        var future = taskScheduler.schedule(() -> {
            var partTimeUsuallyId = punch.getPartTimeUsuallyId();
            var username = punch.getUser().getUsername();
            var password = new String(encryptor.decrypt(Hex.decode(punch.getUser().getPass())));
            var agent = punchAgentFactory.create(partTimeUsuallyId, username, password, punch.getJobDescription());
            try {
                if (taskType == TaskType.CLOCK_IN) {
                    agent.clockIn();
                    punch.setStatus(Punch.Status.CLOCK_IN_SUCCESS);
                } else {
                    agent.clockOut();
                    punch.setStatus(Punch.Status.CLOCK_OUT_SUCCESS);
                }
            } catch (LoginFailedException e) {
                if (taskType == TaskType.CLOCK_IN) {
                    punch.setStatus(Punch.Status.CLOCK_IN_FAILED);
                    log.error("Schedule {} clock in failed with exception: ", punch, e);
                } else {
                    punch.setStatus(Punch.Status.CLOCK_OUT_FAILED);
                    log.error("Schedule {} clock out failed with exception: ", punch, e);
                }
            } catch (ClockInFailedException e) {
                punch.setStatus(Punch.Status.CLOCK_IN_FAILED);
                log.error("Schedule {} clock in failed with exception: ", punch, e);
            } catch (ClockOutFailedException e) {
                punch.setStatus(Punch.Status.CLOCK_OUT_FAILED);
                log.error("Schedule {} clock out failed with exception: ", punch, e);
            } finally {
                repository.save(punch);
                var punchEvent = new IftttPunchEvent(punch);
                taskMap.remove(task);

                publisher.trigger(punchEvent);
            }
        }, taskType == TaskType.CLOCK_IN ? punch.getClockInTime() : punch.getClockOutTime());
        taskMap.put(task, future);
        log.info("Scheduled {} {} at {}", punch, taskType, taskType == TaskType.CLOCK_IN ? punch.getClockInTime() : punch.getClockOutTime());
    }

    public void schedule(Punch punch) {
        schedule(punch, TaskType.CLOCK_IN);
        schedule(punch, TaskType.CLOCK_OUT);
    }

    public void scheduleAll(List<Punch> punches) {
        // Schedule clock in for punches that are not clocked in yet
        punches.stream()
                .filter(punch -> punch.getStatus() == Punch.Status.PENDING)
                .filter(punch -> !punch.isAfterClockInTime())
                .forEach(punch -> {
                    schedule(punch, TaskType.CLOCK_IN);
                    schedule(punch, TaskType.CLOCK_OUT);
                });

        // Schedule clock out for punches that are already clocked in
        punches.stream()
                .filter(punch -> punch.getStatus() == Punch.Status.CLOCK_IN_SUCCESS)
                .filter(punch -> !punch.isAfterClockOutTime())
                .forEach(punch -> schedule(punch, TaskType.CLOCK_OUT));
    }

    public record Task(Punch punch, TaskType taskType) {
    }

    public enum TaskType {
        CLOCK_IN,
        CLOCK_OUT
    }
}