package com.abcgreen.evnet.web;

import com.abcgreen.evnet.config.WebConfig;
import com.abcgreen.evnet.model.Models.User;
import com.abcgreen.evnet.service.AuthService;
import com.abcgreen.evnet.service.Exceptions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest req) {
        return WebConfig.currentUser(req) != null ? "redirect:/dashboard" : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest req, RedirectAttributes ra) {
        try {
            User u = auth.login(email, password);
            req.getSession().setAttribute(WebConfig.SESSION_USER, u);
            return u.isAdmin() ? "redirect:/admin" : "redirect:/dashboard";
        } catch (Exceptions.AuthException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam(required = false) String phone, @RequestParam String password,
                           HttpServletRequest req, RedirectAttributes ra) {
        try {
            User u = auth.register(name, email, phone, password);
            req.getSession().setAttribute(WebConfig.SESSION_USER, u);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req) {
        req.getSession().invalidate();
        return "redirect:/";
    }
}
