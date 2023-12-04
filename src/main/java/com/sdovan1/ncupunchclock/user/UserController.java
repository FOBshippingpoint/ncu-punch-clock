package com.sdovan1.ncupunchclock.user;

import com.sdovan1.ncupunchclock.schedule.PunchAgentFactory;
import com.sdovan1.ncupunchclock.passcode.PasscodeRepository;
import com.sdovan1.ncupunchclock.schedule.PunchLoginFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasscodeRepository passcodeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserPasswordEncryptor userPasswordEncryptor;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    private final Map<String, CompletableFuture<Void>> verifyFutures = new ConcurrentHashMap<>();
    @Autowired
    private PunchAgentFactory punchAgentFactory;

    @InitBinder
    public void setAllowedFields(WebDataBinder binder) {
        binder.setDisallowedFields("id");
    }

    @ModelAttribute("user")
    public User user() {
        return new User();
    }

    @InitBinder("changePasswordDTO")
    public void initChangePasswordDTOValidator(WebDataBinder binder) {
        binder.setValidator(new ChangePasswordDTOValidator());
    }

    @GetMapping("/sign_up")
    public String signUp(Model model) {
        model.addAttribute("user", new User());
        return "sign_up";
    }

    @PostMapping("/sign_up")
    public String register(@Valid User user, Model model, BindingResult result) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("password", "usernameAlreadyExists", "這個帳號已有人使用，請試試其他名稱。");
        }
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            result.rejectValue("password", "passwordsDoNotMatch", "您輸入的兩個密碼並不相符，請再試一次。");
        }
        if (user.getUsername().equals(user.getPassword())) {
            result.rejectValue("password", "passwordSameAsUsername", "您的密碼不能和帳號相同，請再試一次。");
        }
        if (passcodeRepository.findByPasscode(user.getPasscode()).isEmpty()) {
            result.rejectValue("passcode", "passcodeDoesNotExist", "邀請碼錯誤。");
        }
        if (result.hasErrors()) {
            return "sign_up";
        }

        var verifyId = UUID.randomUUID().toString();
        var isPortalAccountValid = verifyPortalAccount(user);
        verifyFutures.put(verifyId, isPortalAccountValid);
        isPortalAccountValid.whenComplete((v, ex) -> {
            if (ex == null) {
                userPasswordEncryptor.setPassword(user, user.getPassword());
                userRepository.save(user);
                passcodeRepository.deleteByPasscode(user.getPasscode());
                log.info("User {} registered", user.getUsername());
                log.info("Passcode {} consumed", user.getPasscode());
            }
        });

        model.addAttribute("verifyId", verifyId);
        return "verify_account";
    }

    public CompletableFuture<Void> verifyPortalAccount(User user) {
        return CompletableFuture.runAsync(() -> {
            var agent = punchAgentFactory.create(user.getUsername(), user.getPassword());
            try {
                agent.login();
            } finally {
                agent.getDriver().quit();
            }
        });
    }

    @GetMapping("/sse/verify_account/{verifyId}")
    public SseEmitter getSseEmitter(@PathVariable String verifyId) {
        var emitter = new SseEmitter();
        if (verifyFutures.containsKey(verifyId)) {
            var future = verifyFutures.get(verifyId);
            future.whenComplete((result, ex) -> {
                var event = SseEmitter.event().name("message");
                if (ex != null) {
                    if (ex.getCause() instanceof PunchLoginFailedException e) {
                        if (e.getMessage().equals(PunchLoginFailedException.FAILED_TO_PASS_CAPTCHA)) {
                            event = event.data("註冊失敗，" + e.getMessage() + "，請通知管理員。");
                        } else if (e.getMessage().equals(PunchLoginFailedException.WRONG_USERNAME_OR_PASSWORD)) {
                            event = event.data("註冊失敗，" + e.getMessage() + "。");
                        } else {
                            event = event.data("註冊失敗，請通知管理員。");
                        }
                    } else {
                        event = event.data("註冊失敗，請通知管理員。");
                        log.error("Unknown exception caught when verifying the user's portal account: ", ex);
                    }
                } else {
                    event = event.data("註冊成功！");
                }
                try {
                    emitter.send(event);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    emitter.complete();
                    verifyFutures.remove(verifyId);
                }
            });
        } else {
            emitter.completeWithError(new RuntimeException("Invalid verifyId"));
        }
        return emitter;
    }

    @PostMapping("/change_password")
    public String changePassword(@Valid ChangePasswordDTO changePasswordDTO, BindingResult result, RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserDetails userDetails, Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        if (result.hasErrors()) {
            return "change_password";
        }
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), userDetails.getPassword())) {
            result.rejectValue("oldPassword", "oldPassword", "舊密碼錯誤");
            return "change_password";
        }
        // this doesn't work: (User) CustomUserDetails
        // would throw exception:
        // org.hibernate.UnknownEntityTypeException: Unable to locate entity descriptor: com.sdovan1.ncupunchclock.user.CustomUserDetails
        var user = userDetails.getUser();
        userPasswordEncryptor.setPassword(user, changePasswordDTO.getNewPassword());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("notification", "密碼變更成功");
        logoutHandler.logout(request, response, authentication);

        log.info("User {} changed password", user.getUsername());
        return "redirect:/login";
    }

    @GetMapping("/change_password")
    public String changePassword(Model model) {
        model.addAttribute("changePasswordDTO", ChangePasswordDTO.builder().build());
        return "change_password";
    }
}
