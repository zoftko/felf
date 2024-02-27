package com.zoftko.felf.tasks;

import com.zoftko.felf.db.dao.AnalysisRepository;
import com.zoftko.felf.db.entities.Analysis;
import com.zoftko.felf.db.entities.Size;
import com.zoftko.felf.services.GithubService;
import freemarker.template.Template;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import reactor.core.publisher.Mono;

@Component
public class PostCommentsTask {

    private final Template pullRequestTemplate;
    private final AnalysisRepository analysisRepository;
    private final GithubService githubService;

    private final Logger log = LoggerFactory.getLogger(PostCommentsTask.class);

    @Autowired
    public PostCommentsTask(
        AnalysisRepository analysisRepository,
        GithubService githubService,
        FreeMarkerConfig config
    ) throws IOException {
        this.analysisRepository = analysisRepository;
        this.githubService = githubService;

        this.pullRequestTemplate = config.getConfiguration().getTemplate("markdown/pull-request.md");
    }

    @Transactional
    @Scheduled(fixedRate = 30_000)
    public void processPendingAnalysisComments() {
        var pendingAnalysis = analysisRepository.findByCommentOrderByCreatedAtAsc(
            Analysis.CommentStatus.TODO,
            Pageable.ofSize(5)
        );
        log.debug("processing {} records", pendingAnalysis.size());

        pendingAnalysis.forEach(this::postAnalysisComment);
    }

    public void postAnalysisComment(Analysis analysis) {
        var model = analysisCommentModel(analysis);
        String body;
        try {
            body = FreeMarkerTemplateUtils.processTemplateIntoString(pullRequestTemplate, model);
        } catch (Exception e) {
            log.error(e.getMessage());
            analysis.setComment(Analysis.CommentStatus.FAIL);
            return;
        }

        var issueId = analysis.getIssueId();
        if (issueId.isEmpty()) {
            log.error(
                "analysis {} with ref {} does not belong to a pull request",
                analysis.getId(),
                analysis.getRef()
            );
            analysis.setComment(Analysis.CommentStatus.FAIL);
            return;
        }

        var nameParts = analysis.getProject().getFullName().split("/");
        var installationId = analysis.getProject().getInstallation().getId();

        if (analysis.getCommentId() != null) {
            log.debug("deleting previous comment {}", analysis.getCommentId());
            var response = githubService
                .deleteIssueComment(installationId, nameParts[0], nameParts[1], analysis.getCommentId())
                .onErrorResume(error -> {
                    log.error(error.getMessage());
                    return Mono.empty();
                })
                .block();
            if (response != null && response.getStatusCode() == HttpStatus.NO_CONTENT) {
                analysis.setCommentId(null);
            }
        }

        var comment = githubService
            .createIssueComment(installationId, issueId.get(), nameParts[0], nameParts[1], body)
            .onErrorResume(error -> {
                log.error(error.getMessage());
                return Mono.empty();
            })
            .block();

        if (comment != null && comment.createdAt() != null) {
            log.debug("successfully created comment {} on pr {}", comment.id(), issueId.get());
            analysis.setComment(Analysis.CommentStatus.NOOP);
            analysis.setCommentId(comment.id());
        } else {
            analysis.setComment(Analysis.CommentStatus.FAIL);
        }
    }

    public Map<String, Object> analysisCommentModel(Analysis analysis) {
        var size = analysis.getSize();
        var model = new HashMap<String, Object>(
            Map.of(
                "prText",
                size.getText(),
                "prData",
                size.getData(),
                "prBss",
                size.getBss(),
                "sha",
                analysis.getSha()
            )
        );

        var project = analysis.getProject();
        var mainAnalysis = analysisRepository.getLastAnalysisByRef(project, project.getDefaultBranch());

        model.put("firstTime", mainAnalysis.isEmpty());
        mainAnalysis.ifPresent(data -> {
            var mainSize = data.getSize();
            model.put("mainText", mainSize.getText());
            model.put("mainData", mainSize.getData());
            model.put("mainBss", mainSize.getBss());

            List
                .of("Text", "Data", "Bss")
                .forEach(category -> {
                    var main = (long) model.get("main" + category);
                    var pr = (long) model.get("pr" + category);

                    if (main == 0 || pr == 0) {
                        return;
                    }
                    model.put("diff" + category, Size.percentDiff(main, pr));
                });
        });

        return model;
    }
}
