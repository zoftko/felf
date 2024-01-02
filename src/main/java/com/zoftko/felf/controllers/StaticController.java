package com.zoftko.felf.controllers;

import com.zoftko.felf.config.SecurityConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaticController {

    public static final String LOGIN_MAPPING = "/login";

    @GetMapping(LOGIN_MAPPING)
    public String login(
        HttpServletRequest request,
        @RequestParam(name = "signup", required = false) String signUp,
        Model model
    ) {
        String message;
        if (signUp != null) {
            message = "No signup required, simply log in with your GitHub account to get started";
        } else {
            request
                .getSession()
                .setAttribute(SecurityConfiguration.SESSION_REDIRECT_ATTR, request.getHeader("Referer"));
            message = "Welcome back, please log in";
        }
        model.addAttribute("welcomeMessage", message);

        return "login";
    }
}
