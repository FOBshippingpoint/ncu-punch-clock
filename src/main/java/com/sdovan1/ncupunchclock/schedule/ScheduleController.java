package com.sdovan1.ncupunchclock.schedule;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
public class ScheduleController {

    @Autowired
    private PunchRepository punchRepository;

    @Autowired
    private PunchScheduler punchScheduler;

    @ModelAttribute("scheduleDTO")
    public ScheduleDTO scheduleDTO() {
        return ScheduleDTO.builder().build();
    }

    @ModelAttribute("punchDTO")
    public PunchDTO punchDTO() {
        return PunchDTO.builder().build();
    }

    @InitBinder("scheduleDTO")
    public void initScheduleDTOValidator(WebDataBinder binder) {
        binder.addValidators(new ScheduleDTOValidator());
    }

    @GetMapping("/schedules/new")
    public String newSchedule(Model model) {
        return "schedule_new";
    }

    @ModelAttribute("sixYearMonthsFromNow")
    public List<YearMonth> sixYearMonthsFromNow() {
        var yearMonthNow = YearMonth.now();
        return List.of(
                yearMonthNow,
                yearMonthNow.plusMonths(1),
                yearMonthNow.plusMonths(2),
                yearMonthNow.plusMonths(3),
                yearMonthNow.plusMonths(4),
                yearMonthNow.plusMonths(5)
        );
    }

    @PostMapping("/schedules/new")
    public String processCreationForm(@Valid ScheduleDTO scheduleDTO, BindingResult result, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (result.hasErrors()) {
            return "schedule_new";
        }

        var punches = scheduleDTO.toPunchList();
        punches.forEach(punch -> punch.setUser(userDetails.getUser()));
        var savedPunches = punchRepository.saveAll(punches);
        punchScheduler.scheduleAll(savedPunches);

        return "redirect:/schedules";
    }

    @PostMapping("schedules/edit/{id}")
    public String updatePunch(@PathVariable Long id, @Valid PunchDTO punchDTO, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "schedule_detail";
        }
        var punch = punchRepository.findById(id).orElseThrow();
        if (!punch.isEditable()) {
            throw new PunchCannotEditException("無法編輯此排程");
        }

        if (punchDTO.getClockInTime() != null && punchDTO.getClockOutTime() != null && punchDTO.getClockInTime() >= punchDTO.getClockOutTime()) {
            result.rejectValue("clockInTime", "clockInTime", "上班時間必須早於下班時間");
        }
        if (punchDTO.getDate() != null && punchDTO.getDate().isBefore(LocalDate.now())) {
            result.rejectValue("date", "date", "日期不得早於今天");
        }
        punchScheduler.cancelScheduledPunch(punch);

        var newPunch = punchDTO.toPunch();
        newPunch.setId(id);
        newPunch.setUser(punch.getUser());
        punchScheduler.schedule(newPunch);
        punchRepository.save(newPunch);
        redirectAttributes.addFlashAttribute("notification", "排程已更新");
        return "redirect:/schedules";
    }

    @PostMapping("/schedules/delete/{id}")
    @PreAuthorize("@punchService.checkIfUserIsOwner(#id, principal)")
    public String deletePunch(@PathVariable Long id) {
        var punch = punchRepository.findById(id).orElseThrow();
        punchScheduler.cancelScheduledPunch(punch);
        punchRepository.deleteById(id);
        return "redirect:/schedules";
    }

    @GetMapping("/schedules/{id}")
    @PreAuthorize("@punchService.checkIfUserIsOwner(#id, principal)")
    public String getPunch(@PathVariable("id") Long id, Model model) {
        var punch = punchRepository.findById(id).orElseThrow();
        model.addAttribute("punchDTO", new PunchDTO(punch));
        return "schedule_detail";
    }

    @GetMapping("/schedules")
    public String getPunches(@RequestParam(required = false) String username, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Punch> punches;
        if (userDetails.isAdmin()) {
            if (username == null || username.isBlank()) {
                punches = punchRepository.findAllByOrderByStatusAscClockInTime();
            } else {
                punches = punchRepository.findByUser_UsernameOrderByStatusAscClockInTime(username);
            }
        } else {
            punches = punchRepository.findByUserOrderByStatusAscClockInTime(userDetails.getUser());
        }
        model.addAttribute("punches", punches);
        return "schedule_list";
    }

    @ModelAttribute("punchQuery")
    public PunchQuery punchQuery() {
        return PunchQuery.builder().build();
    }
}
