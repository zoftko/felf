package com.zoftko.felf.controllers;

import com.zoftko.felf.db.dao.AnalysisRepository;
import com.zoftko.felf.services.FelfService;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RepositoryController {

    private static final String MKEY = "measurements";
    private final FelfService felfService;
    private final AnalysisRepository analysisRepository;
    private final SecureRandom secureRandom;
    private final PasswordEncoder encoder;

    private final Logger log = LoggerFactory.getLogger(RepositoryController.class);

    @Autowired
    public RepositoryController(
        FelfService felfService,
        AnalysisRepository analysisRepository,
        SecureRandom secureRandom,
        PasswordEncoder encoder
    ) {
        this.felfService = felfService;
        this.analysisRepository = analysisRepository;
        this.secureRandom = secureRandom;
        this.encoder = encoder;
    }

    private void initModel(String owner, String repo, Model model) {
        model.addAttribute("owner", owner);
        model.addAttribute("repo", repo);

        model.addAttribute("error", false);
        model.addAttribute("errorMessage", "None");

        model.addAttribute("token", "");
    }

    @GetMapping("/{owner}/{repo}")
    public String getRepository(
        Principal principal,
        @PathVariable String owner,
        @PathVariable String repo,
        Model model
    ) {
        initModel(owner, repo, model);

        var projectData = felfService.getProjectData(owner, repo);
        model.addAttribute("projectPresent", projectData.project().isPresent());
        model.addAttribute(
            "isOwner",
            principal != null && projectData.isOwner(Integer.parseInt(principal.getName()))
        );
        model.addAttribute("hasPermissions", projectData.hasPermissions());
        model.addAttribute("fullName", projectData.fullName());
        model.addAttribute(
            "isPrivate",
            projectData.repository() != null && projectData.repository().isPrivate()
        );

        projectData
            .project()
            .ifPresent(project -> {
                model.addAttribute("project", project);
                analysisRepository
                    .getLastAnalysisByRef(project, project.getDefaultBranch())
                    .ifPresent(analysis ->
                        model.addAttribute(
                            MKEY,
                            Map.of(
                                "Text Size",
                                analysis.getSize().getText(),
                                "Data Size",
                                analysis.getSize().getData(),
                                "BSS Size",
                                analysis.getSize().getBss()
                            )
                        )
                    );
            });

        model.addAttribute("demo", !model.containsAttribute(MKEY));
        if (!model.containsAttribute(MKEY)) {
            model.addAttribute(MKEY, Map.of("Text Size", 58432, "Data Size", 2048, "BSS Size", 8321));
        }

        return "pages/repository/index";
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER')")
    @PostMapping("{owner}/{repo}/token")
    public String createRepositoryToken(
        Principal principal,
        @PathVariable String owner,
        @PathVariable String repo,
        Model model
    ) throws ResponseStatusException {
        initModel(owner, repo, model);

        var projectData = felfService.getFreshProjectData(owner, repo);
        if (!projectData.isOwner(Integer.parseInt(principal.getName()))) {
            throw new ResponseStatusException(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
        }
        if (projectData.repository() == null || projectData.repository().isPrivate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        var project = projectData.project().orElse(projectData.initializeProject());
        var token = project.generateToken(secureRandom, encoder);
        model.addAttribute("token", token);

        log.info("user {} generated token for {}", principal.getName(), project.getFullName());
        felfService.storeProject(project);

        return "pages/repository/token";
    }
}
