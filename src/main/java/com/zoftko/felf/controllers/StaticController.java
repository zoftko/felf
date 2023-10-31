package com.zoftko.felf.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaticController {

    public static final String LOGIN_MAPPING = "/login";

    @GetMapping(LOGIN_MAPPING)
    public String login(@RequestParam(name = "signup", required = false) String signUp, Model model) {
        String message;
        if (signUp != null) {
            message = "No signup required, simply log in with your GitHub account to get started";
        } else {
            message = "Welcome back, please log in";
        }
        model.addAttribute("welcomeMessage", message);

        return "login";
    }
}
