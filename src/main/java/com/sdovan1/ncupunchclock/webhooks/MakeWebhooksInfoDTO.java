package com.sdovan1.ncupunchclock.webhooks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeWebhooksInfoDTO {
    private String url;

    public String getUrl() {
        return Objects.requireNonNullElse(url, "");
    }
}
