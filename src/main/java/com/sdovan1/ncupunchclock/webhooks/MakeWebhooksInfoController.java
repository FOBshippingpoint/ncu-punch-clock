package com.sdovan1.ncupunchclock.webhooks;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Controller
@PreAuthorize("hasRole('ROLE_USER')")
public class MakeWebhooksInfoController {
    private final WebhooksInfoRepository webhooksInfoRepository;
    private final MakeWebhooksPublisher webhooksPublisher;

    @Autowired
    public MakeWebhooksInfoController(WebhooksInfoRepository webhooksInfoRepository, MakeWebhooksPublisher webhooksPublisher) {
        this.webhooksInfoRepository = webhooksInfoRepository;
        this.webhooksPublisher = webhooksPublisher;
    }

    @GetMapping("/change_make_webhooks")
    public String getMakeWebhooksUpdateForm() {
        return "change_make_webhooks";
    }

    @PostMapping("/change_make_webhooks")
    public String updateMakeWebhooks(@AuthenticationPrincipal CustomUserDetails userDetails, MakeWebhooksInfoDTO makeWebhooksInfo, Model model) {
        try {
            new URL(makeWebhooksInfo.getUrl()).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            model.addAttribute("notification", "請輸入Make Webhooks URL");
            return "change_make_webhooks";
        }
        var webhooksInfo = webhooksInfoRepository.findByUser(userDetails.getUser()).orElseGet(WebhooksInfo::new);
        var adaptedWebhooksKeyDTO = MakeWebhooksInfoAdapter.toEntity(makeWebhooksInfo);
        webhooksInfo.setUser(userDetails.getUser());
        webhooksInfo.setInfo(adaptedWebhooksKeyDTO.getInfo());
        webhooksInfoRepository.save(webhooksInfo);
        webhooksPublisher.trigger(makeWebhooksInfo.getUrl(), "NCU打卡魔法師通知測試");
        model.addAttribute("notification", "Make Webhooks已更新，請確認您選擇的App是否收到通知測試。");
        return "change_make_webhooks";
    }

    @ModelAttribute("makeWebhooksInfoDTO")
    public MakeWebhooksInfoDTO makeWebhooksKeyDTO() {
        return new MakeWebhooksInfoDTO();
    }
}
