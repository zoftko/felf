package com.zoftko.felf.controllers;

import com.zoftko.felf.db.dao.AnalysisRepository;
import com.zoftko.felf.db.dao.ProjectRepository;
import com.zoftko.felf.db.entities.Analysis;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    public static final String MAPPING = "/api/analysis";
    public static final String HEADER_REPO = "X-Felf-Repo";

    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;

    private final PasswordEncoder encoder;

    private final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    public AnalysisController(
        ProjectRepository projectRepository,
        AnalysisRepository analysisRepository,
        PasswordEncoder encoder
    ) {
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
        this.encoder = encoder;
    }

    @PostMapping(MAPPING)
    public ResponseEntity<Object> storeAnalysis(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestHeader(HEADER_REPO) String repo,
        @RequestBody Analysis analysis
    ) {
        if (!authorization.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        var project = projectRepository.findByFullName(repo);
        if (project.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!encoder.matches(authorization.replaceFirst("Bearer ", ""), project.get().getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        analysis.setProject(project.get());
        analysis.setCreatedAt(LocalDateTime.now());

        if (analysis.getRef().endsWith("/merge")) {
            analysis.setComment(Analysis.CommentStatus.TODO);
            analysisRepository
                .findByProjectAndRef(analysis.getProject(), analysis.getRef())
                .ifPresent(value -> {
                    analysis.setId(value.getId());
                    analysis.setCommentId(value.getCommentId());
                });
        }

        if (log.isInfoEnabled()) {
            // Should not need replaceAll but better safe than sorry...
            log.info(
                "saving analysis project={} sha={}",
                project.get().getFullName(),
                analysis.getSha().replaceAll("[\n\r]", "")
            );
        }
        analysisRepository.save(analysis);

        return ResponseEntity.ok().build();
    }
}
