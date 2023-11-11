package com.zoftko.felf.controllers;

import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.models.InstallationRepos;
import com.zoftko.felf.services.GithubService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AppController {

    private final GithubService githubService;

    @Autowired
    public AppController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/")
    public String getDashboard(Principal principal, Model model) {
        List<Installation> installations = githubService.getUserInstallations(
            Integer.parseInt(principal.getName())
        );
        model.addAttribute("installations", installations);
        model.addAttribute("defaultInstallation", installations.isEmpty() ? null : installations.getFirst());

        return "dashboard";
    }

    @GetMapping("/fragments/{installationId}/projects")
    public String getProjects(
        @PathVariable Integer installationId,
        @AuthenticationPrincipal OAuth2User principal,
        Model model
    ) {
        if (
            githubService.userForbiddenFromInstallation(Integer.parseInt(principal.getName()), installationId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        model.addAttribute("projects", githubService.getInstallationProjects(installationId));
        model.addAttribute("installationId", installationId);

        return "fragments/projectTable";
    }

    @GetMapping("/fragments/{installationId}/repos")
    public String getRepos(
        @PathVariable Integer installationId,
        @AuthenticationPrincipal OAuth2User principal,
        @RequestParam(defaultValue = "" + GithubService.DEFAULT_PAGE) int page,
        @RequestParam(defaultValue = "" + GithubService.DEFAULT_PAGE_SIZE) int size,
        Model model
    ) {
        if (
            githubService.userForbiddenFromInstallation(Integer.parseInt(principal.getName()), installationId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var response = Optional.ofNullable(
            githubService.getInstallationRepositories(installationId, page, size).block()
        );
        model.addAttribute("totalCount", response.map(InstallationRepos::totalCount).orElse(0));
        model.addAttribute("repos", response.map(InstallationRepos::repositories).orElse(null));

        return "addRepoModal";
    }
}
