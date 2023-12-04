package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.bot.Publisher;
import com.sdovan1.ncupunchclock.schedule.PunchEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// IFTTT is the weirdest api I have ever seen
// In Webhooks trigger, choose "Receive a web request" instead of "Receive a web request with a JSON payload"
// so you can "finally" use {{Value1}}, {{Value2}}, and {{Value3}} in the Ingredients
// visit https://ifttt.com/maker_webhooks for more info
@Component
public class IftttWebhooksPublisher implements Publisher {
    private final IftttKeyRepository iftttKeyRepository;

    private static final String EVENT_NAME = "ncu_punch_clock";
    private static final String IFTTT_WEBHOOKS_BASE_URL = "https://maker.ifttt.com";

    public IftttWebhooksPublisher(IftttKeyRepository iftttKeyRepository) {
        this.iftttKeyRepository = iftttKeyRepository;
    }

    public void trigger(IftttPunchEvent punchEvent) {
        var key = iftttKeyRepository
                .findByUser(punchEvent.getUser())
                .orElseThrow(
                        () -> new IftttTriggerException("No IFTTT key found for user " + punchEvent.getUser().getUsername())
                );
        trigger(key.getIftttWebhooksKey(), punchEvent.getSummary());
    }

    @Override
    public <T extends PunchEvent> void trigger(T punchEvent) {
        if (punchEvent instanceof IftttPunchEvent) {
            trigger(punchEvent);
        } else {
            throw new IllegalArgumentException("punchEvent must be an instance of IftttPunchEvent");
        }
    }

    public void trigger(String iftttWebhooksKey, String message) {
        trigger(iftttWebhooksKey, new IftttBody(message));
    }

    public void trigger(String iftttWebhooksKey, Object body) {
        var uri = IFTTT_WEBHOOKS_BASE_URL + "/trigger/" + EVENT_NAME + "/with/key/" + iftttWebhooksKey;
        RestClient.create()
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve();
    }

    record IftttBody(String value1) {
    }
}
