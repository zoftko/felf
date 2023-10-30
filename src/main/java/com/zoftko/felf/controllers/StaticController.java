package com.zoftko.felf.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {

    public static final String LOGIN_MAPPING = "/login";

    private static final Map<String, String> oAuthProviderMap = new HashMap<>();

    static {
        oAuthProviderMap.put("github", "GitHub");
    }

    @GetMapping(LOGIN_MAPPING)
    public String login(Model model) {
        model.addAttribute("providers", oAuthProviderMap);
        return "login";
    }
}
