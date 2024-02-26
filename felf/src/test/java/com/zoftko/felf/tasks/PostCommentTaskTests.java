package com.zoftko.felf.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.zoftko.felf.dao.AnalysisRepository;
import com.zoftko.felf.entities.Analysis;
import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import com.zoftko.felf.entities.Size;
import com.zoftko.felf.models.CreateIssueComment;
import com.zoftko.felf.services.GithubService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = FreeMarkerAutoConfiguration.class)
class PostCommentTaskTests {

    @MockBean
    AnalysisRepository analysisRepository;

    @MockBean
    GithubService githubService;

    @Autowired
    FreeMarkerConfig config;

    PostCommentsTask postCommentsTask;

    static final int installId = 1234;
    static Installation installation;

    static final String projectName = "zoftko/felf";
    static final String defaultBranch = "main";
    static final String sha = "6a562bec11143b54da9730f7932eb877067d29f7";
    static Project project;

    @BeforeAll
    static void setUpAll() {
        installation = new Installation();
        installation.setId(installId);

        project = new Project();
        project.setInstallation(installation);
        project.setFullName(projectName);
        project.setDefaultBranch(defaultBranch);
    }

    @BeforeEach
    void setUp() throws IOException {
        postCommentsTask = new PostCommentsTask(analysisRepository, githubService, config);
    }

    @Test
    void analysisCommentModelNoComparison() {
        var size = new Size();
        size.setText(1L);
        size.setData(2L);
        size.setBss(33L);

        var targetAnalysis = new Analysis();
        targetAnalysis.setSize(size);
        targetAnalysis.setProject(project);
        targetAnalysis.setSha(sha);

        when(analysisRepository.getLastAnalysisByRef(project, "main")).thenReturn(Optional.empty());

        var model = postCommentsTask.analysisCommentModel(targetAnalysis);
        assertThat(model)
            .containsKey("sha")
            .containsEntry("prText", 1L)
            .containsEntry("prData", 2L)
            .containsEntry("prBss", 33L)
            .containsEntry("firstTime", true);
    }

    @Test
    void analysisCommentModelWithComparison() {
        var size = new Size();
        size.setText(300L);
        size.setData(2L);
        size.setBss(3L);

        var targetAnalysis = new Analysis();
        targetAnalysis.setSize(size);
        targetAnalysis.setProject(project);
        targetAnalysis.setSha(sha);

        var mainSize = new Size();
        mainSize.setText(150L);
        mainSize.setData(2L);
        mainSize.setBss(6L);

        var mainAnalysis = new Analysis();
        mainAnalysis.setSize(mainSize);

        when(analysisRepository.getLastAnalysisByRef(project, "main")).thenReturn(Optional.of(mainAnalysis));

        var model = postCommentsTask.analysisCommentModel(targetAnalysis);
        assertThat(model)
            .containsEntry("firstTime", false)
            .containsEntry("diffText", "+100.00%")
            .containsEntry("diffData", "+0.00%")
            .containsEntry("diffBss", "-50.00%");
    }

    @Test
    void processPendingAnalysisComments() {
        var size = new Size();
        size.setText(1L);
        size.setData(2L);
        size.setBss(1024L);

        var targetAnalysis = new Analysis();
        targetAnalysis.setSha(sha);
        targetAnalysis.setRef("1025/merge");
        targetAnalysis.setSize(size);
        targetAnalysis.setProject(project);
        targetAnalysis.setComment(Analysis.CommentStatus.TODO);
        targetAnalysis.setCommentId(35L);

        when(analysisRepository.findByCommentOrderByCreatedAtAsc(eq(Analysis.CommentStatus.TODO), any()))
            .thenReturn(List.of(targetAnalysis));
        when(githubService.deleteIssueComment(installId, "zoftko", "felf", 35L))
            .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build()));
        when(
            githubService.createIssueComment(eq(installId), eq("1025"), eq("zoftko"), eq("felf"), anyString())
        )
            .thenReturn(Mono.just(new CreateIssueComment(99L, LocalDateTime.now())));

        postCommentsTask.processPendingAnalysisComments();

        verify(githubService, times(1)).deleteIssueComment(installId, "zoftko", "felf", 35L);
        assertThat(targetAnalysis.getComment()).isEqualTo(Analysis.CommentStatus.NOOP);
        assertThat(targetAnalysis.getCommentId()).isEqualTo(99L);
    }
}
