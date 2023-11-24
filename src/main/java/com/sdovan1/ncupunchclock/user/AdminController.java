package com.sdovan1.ncupunchclock.user;

import com.sdovan1.ncupunchclock.passcode.Passcode;
import com.sdovan1.ncupunchclock.passcode.PasscodeDTO;
import com.sdovan1.ncupunchclock.passcode.PasscodeRepository;
import com.sdovan1.ncupunchclock.schedule.PunchRepository;
import com.sdovan1.ncupunchclock.schedule.PunchScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PunchScheduler punchScheduler;
    @Autowired
    private UserPasswordEncryptor userPasswordEncryptor;
    @Autowired
    private PunchRepository punchRepository;
    @Autowired
    private PasscodeRepository passcodeRepository;

    @PostMapping("/users/reset_password/{username}")
    public String resetPassword(@PathVariable String username, RedirectAttributes redirectAttributes, @Value("${reset-password}") String resetPassword) {
        var user = userRepository.findByUsername(username).orElseThrow();
        userPasswordEncryptor.setPassword(user, resetPassword);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("notification", username + "已成功重設密碼爲" + resetPassword);
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{username}")
    public String deleteUser(@PathVariable String username, RedirectAttributes redirectAttributes) {
        if (username.equals("admin")) {
            redirectAttributes.addFlashAttribute("notification", "不能刪除admin");
            return "redirect:/admin/users";
        }
        var user = userRepository.findByUsername(username).orElseThrow();
        punchScheduler.cancelAllScheduledPunches(user.getPunches());
        punchRepository.deleteAll(user.getPunches());
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("notification", username + "已成功刪除");
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user_list";
    }

    @PostMapping("/passcodes/new")
    public String newPasscode(PasscodeDTO passcodeDTO, RedirectAttributes redirectAttributes) {
        var passcode = new Passcode(passcodeDTO.getPasscode());
        passcodeRepository.save(passcode);
        redirectAttributes.addFlashAttribute("notification", "新的邀請碼爲" + passcode.getPasscode());
        return "redirect:/admin/passcodes";
    }

    @GetMapping("/passcodes")
    public String getPasscodes(Model model) {
        model.addAttribute("passcodes", passcodeRepository.findAll());
        model.addAttribute("passcodeDTO", new PasscodeDTO());
        return "passcode_list";
    }
}