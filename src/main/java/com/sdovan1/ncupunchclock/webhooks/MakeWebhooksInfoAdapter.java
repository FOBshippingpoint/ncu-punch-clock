package com.sdovan1.ncupunchclock.webhooks;

public class MakeWebhooksInfoAdapter {
    public static MakeWebhooksInfoDTO toDTO(WebhooksInfo webhooksInfo) {
        return new MakeWebhooksInfoDTO(webhooksInfo.getInfo());
    }

    public static WebhooksInfo toEntity(MakeWebhooksInfoDTO makeWebhooksInfoDTO) {
        var webhooksInfo = new WebhooksInfo();
        webhooksInfo.setInfo(makeWebhooksInfoDTO.getUrl());
        return webhooksInfo;
    }
}
