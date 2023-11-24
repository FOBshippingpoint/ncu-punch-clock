package com.sdovan1.ncupunchclock.ifttt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IftttKeyDTO {
    private String iftttWebhooksKey;

    public String getIftttWebhooksKey() {
        return Objects.requireNonNullElse(iftttWebhooksKey, "");
    }
}
