package com.zoftko.felf.controllers;

import static com.zoftko.felf.config.SecurityConfiguration.AUTHORITY_OAUTH2_USER;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.services.GithubService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = AppController.class)
class AppControllerTests extends BaseControllerTest {

    @MockBean
    GithubService githubService;

    static final int userId = 12345;
    OAuth2User user = new DefaultOAuth2User(
        AuthorityUtils.createAuthorityList(AUTHORITY_OAUTH2_USER),
        Map.of("name", Integer.toString(userId), "login", "DummyUser"),
        "name"
    );

    @Test
    void getDashboardNoInstallations() throws Exception {
        mockMvc
            .perform(get("/").with(oauth2Login().oauth2User(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("please install the application")));
    }

    @Test
    void getDashboardWithInstallations() throws Exception {
        var installations = List.of(
            new Installation(1, 23, 54, "user", "LeTufe", userId),
            new Installation(4, 45, 56, "org", "RandomProject", userId)
        );
        when(githubService.getUserInstallations(userId)).thenReturn(installations);

        mockMvc
            .perform(get("/").with(oauth2Login().oauth2User(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("LeTufe")))
            .andExpect(content().string(containsString("data-account=\"23\"")))
            .andExpect(content().string(containsString("RandomProject")));
    }
}
