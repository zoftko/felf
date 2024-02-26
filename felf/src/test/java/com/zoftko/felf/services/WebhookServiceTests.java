package com.zoftko.felf.services;

import static com.zoftko.felf.services.WebhookService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.dao.ProjectRepository;
import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import jakarta.persistence.EntityManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@DataJpaTest
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WebhookServiceTests {

    @Autowired
    EntityManager manager;

    @Autowired
    InstallationRepository installationRepository;

    @Autowired
    ProjectRepository projectRepository;

    @MockBean
    CacheManager cacheManager;

    WebhookService webhookService;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(installationRepository, projectRepository, cacheManager);
    }

    JsonNode createInstallationJsonNode(
        int installationId,
        int accountId,
        int targetId,
        int senderId,
        String accountLogin
    ) throws JSONException, JsonProcessingException {
        JSONObject payload = new JSONObject()
            .put("action", WebhookService.ACTION_CREATED)
            .put(
                "installation",
                new JSONObject()
                    .put("id", installationId)
                    .put("account", new JSONObject().put("id", accountId).put("login", accountLogin))
                    .put("target_id", targetId)
                    .put("target_type", "User")
            )
            .put("sender", new JSONObject().put("id", senderId));

        return objectMapper.readTree(payload.toString());
    }

    Project createProject() {
        var install = new Installation();
        install.setId(12345);
        install.setAccount(1);
        install.setSender(234);
        install.setTarget(12312);
        install.setTargetType("user");
        install.setAccountLogin("test");
        install = installationRepository.save(install);

        var project = new Project();
        project.setToken("dummy");
        project.setDefaultBranch("dev");
        project.setFullName("zoftko/zynth53");
        project.setInstallation(install);

        return project;
    }

    @Test
    void processInstallationInstalled() throws JSONException, JsonProcessingException {
        int installationId = 12345;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(installationId, 1123, 42312, 56, "bach")
        );
        var installationOpt = installationRepository.findById(installationId);
        assertThat(installationOpt).isPresent();

        var installation = installationOpt.get();
        assertThat(installation.getSender()).isEqualTo(56);
        assertThat(installation.getAccount()).isEqualTo(1123);
        assertThat(installation.getTarget()).isEqualTo(42312);
        assertThat(installation.getTargetType()).isEqualTo("User");
        assertThat(installation.getAccountLogin()).isEqualTo("bach");
    }

    @Test
    void processInstallationDeleted() throws JSONException, JsonProcessingException {
        int installationId = 98765;
        assertThat(installationRepository.findById(installationId)).isNotPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            createInstallationJsonNode(installationId, 1234, 5678, 987, "anton")
        );
        assertThat(installationRepository.findById(installationId)).isPresent();

        webhookService.processEvent(
            WebhookService.EVENT_INSTALLATION,
            objectMapper.readTree(
                new JSONObject()
                    .put("action", ACTION_DELETED)
                    .put("installation", new JSONObject().put("id", installationId))
                    .toString()
            )
        );
        assertThat(installationRepository.findById(installationId)).isNotPresent();
    }

    @Test
    void processBadPayload(CapturedOutput output) throws JSONException, JsonProcessingException {
        int installationId = 12345;
        webhookService.processEvent(
            EVENT_INSTALLATION,
            objectMapper.readTree(
                new JSONObject()
                    .put("action", ACTION_CREATED)
                    .put("installation", new JSONObject().put("what", "boom").put("id", installationId))
                    .toString()
            )
        );

        assertThat(output.getOut()).contains("invalid payload for installation.created");
        assertThat(installationRepository.findById(installationId)).isNotPresent();
    }

    @Test
    void processBadAction(CapturedOutput output) throws JSONException, JsonProcessingException {
        webhookService.processEvent(
            EVENT_INSTALLATION,
            objectMapper.readTree(new JSONObject().put("action", "bazinga").toString())
        );

        assertThat(output.getOut()).contains("installation.bazinga is not supported");
    }

    @Test
    void processRepositoryEditDefaultBranch() throws JsonProcessingException {
        var project = projectRepository.save(createProject());
        webhookService.processEvent(
            EVENT_REPOSITORY,
            objectMapper.readTree(
                String.format(
                    """
                    {
                        "action": "edited",
                        "repository": {
                            "full_name": "%s",
                            "default_branch": "main"
                        }
                    }
                    """,
                    project.getFullName()
                )
            )
        );

        manager.clear();
        var updatedProject = projectRepository.findById(project.getId());
        assertThat(updatedProject).isPresent();
        assertThat(updatedProject.get().getDefaultBranch()).isEqualTo("main");
    }

    @Test
    void processRepositoryPrivatized() throws JsonProcessingException {
        var project = projectRepository.save(createProject());
        assertThat(project.getPrivate()).isFalse();

        webhookService.processEvent(
            EVENT_REPOSITORY,
            objectMapper.readTree(
                String.format(
                    """
                    {
                        "action": "privatized",
                        "repository": {
                            "full_name": "%s"
                        }
                    }
                    """,
                    project.getFullName()
                )
            )
        );

        manager.clear();
        assertThat(projectRepository.findById(project.getId()).get().getPrivate()).isTrue();
    }

    @Test
    void processRepositoryPublicized() throws JsonProcessingException {
        var project = createProject();
        project.setPrivate(true);
        project = projectRepository.save(project);
        assertThat(project.getPrivate()).isTrue();

        webhookService.processEvent(
            EVENT_REPOSITORY,
            objectMapper.readTree(
                String.format(
                    """
                    {
                        "action": "publicized",
                        "repository": {
                            "full_name": "%s"
                        }
                    }
                    """,
                    project.getFullName()
                )
            )
        );

        manager.clear();
        assertThat(projectRepository.findById(project.getId()).get().getPrivate()).isFalse();
    }

    @Test
    void processInstallationRepositoriesAdded() throws JsonProcessingException {
        var cache = mock(Cache.class);
        String projectOne = "zoftko/felf";
        String projectTwo = "zoftko/felf-cli";

        when(cacheManager.getCache(FelfService.CACHE_NAME)).thenReturn(cache);
        webhookService.processEvent(
            EVENT_INSTALLATION_REPOS,
            objectMapper.readTree(
                String.format(
                    """
                    {
                        "action": "added",
                        "repositories_added": [
                            {
                              "full_name": "%s"
                            },
                            {
                              "full_name": "%s"
                            }
                        ]
                    }
                    """,
                    projectOne,
                    projectTwo
                )
            )
        );

        verify(cache, times(1)).evictIfPresent(projectOne);
        verify(cache, times(1)).evictIfPresent(projectTwo);
    }
}
