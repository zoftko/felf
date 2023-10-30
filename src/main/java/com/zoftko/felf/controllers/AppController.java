package com.zoftko.felf.controllers;

import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.services.GithubService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    private final GithubService githubService;

    @Autowired
    public AppController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/")
    public String getDashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        List<Installation> installations = githubService.getUserInstallations(
            Integer.parseInt(principal.getName())
        );
        model.addAttribute("installations", installations);

        return "dashboard";
    }
}
