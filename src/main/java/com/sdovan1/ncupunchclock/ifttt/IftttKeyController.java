package com.sdovan1.ncupunchclock.ifttt;

import com.sdovan1.ncupunchclock.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.ConsoleHandler;

@Controller
//@PreAuthorize("hasRole('ROLE_USER')")
public class IftttKeyController {
    private final IftttKeyRepository iftttKeyRepository;
    private final IftttWebhooksPublisher iftttWebhooksPublisher;

    @Autowired
    public IftttKeyController(IftttKeyRepository iftttKeyRepository, IftttWebhooksPublisher iftttWebhooksPublisher) {
        this.iftttKeyRepository = iftttKeyRepository;
        this.iftttWebhooksPublisher = iftttWebhooksPublisher;
    }

    @GetMapping("/change_ifttt_key")
    public String getIftttKeyUpdateForm() {
        return "change_ifttt_key";
    }

    @PostMapping("/change_ifttt_key")
    public String updateIftttKey(@AuthenticationPrincipal CustomUserDetails userDetails, IftttKeyDTO iftttKeyDTO, Model model) {
        var iftttKey = iftttKeyRepository.findByUser(userDetails.getUser()).orElseGet(IftttKey::new);
        iftttKey.setIftttWebhooksKey(iftttKeyDTO.getIftttWebhooksKey());
        iftttKeyRepository.save(iftttKey);
        iftttWebhooksPublisher.trigger(iftttKeyDTO.getIftttWebhooksKey(), "NCU打卡魔法師通知測試");
        model.addAttribute("notification", "IFTTT Webhooks Key已更新，請確認您的Applet是否收到通知測試。");
        return "change_ifttt_key";
    }

    @ModelAttribute("iftttKeyDTO")
    public IftttKeyDTO iftttKeyDTO() {
        return new IftttKeyDTO();
    }
}
