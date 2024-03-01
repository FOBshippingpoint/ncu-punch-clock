package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.bot.Publisher;
import com.sdovan1.ncupunchclock.schedule.PunchEvent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class MakeWebhooksPublisher implements Publisher {
    private final WebhooksInfoRepository webhooksInfoRepository;

    public MakeWebhooksPublisher(WebhooksInfoRepository webhooksInfoRepository) {
        this.webhooksInfoRepository = webhooksInfoRepository;
    }

    public void trigger(WebhooksPunchEvent punchEvent) {
        var webhooksInfo = webhooksInfoRepository
                .findByUser(punchEvent.getUser())
                .orElseThrow(
                        () -> new WebhooksTriggerException("No webhooks found for user " + punchEvent.getUser().getUsername())
                );
        var makeWebhooksDTO = MakeWebhooksInfoAdapter.toDTO(webhooksInfo);
        trigger(makeWebhooksDTO.getUrl(), punchEvent.getSummary());
    }

    @Override
    public <T extends PunchEvent> void trigger(T punchEvent) {
        if (punchEvent instanceof WebhooksPunchEvent webhooksPunchEvent) {
            trigger(webhooksPunchEvent);
        } else {
            throw new IllegalArgumentException("The punchEvent must be an instance of " + WebhooksPunchEvent.class.getName() + ".");
        }
    }

    public void trigger(String makeUrl, String message) {
        RestClient.create()
                .get()
                .uri(makeUrl, Map.of("message", message))
                .retrieve();
    }
}