package aegis.server.tempoary;

import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/user-info")
    public String userInfo(@LoginUser SessionUser user, Model model) {
        if (user == null) {
            return "redirect:/login-fail";
        }

        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/login-fail")
    public String loginFail() {
        return "login-fail";
    }
}
