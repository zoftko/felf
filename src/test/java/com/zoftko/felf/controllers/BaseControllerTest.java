package com.zoftko.felf.controllers;

import static com.zoftko.felf.config.SecurityConfiguration.AUTHORITY_OAUTH2_USER;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

import com.zoftko.felf.config.SecurityConfiguration;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = { SecurityConfiguration.class })
public abstract class BaseControllerTest {

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    MockMvc mockMvc;

    OAuth2User mockGithubUser(Integer userId) {
        return new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList(AUTHORITY_OAUTH2_USER),
            Collections.singletonMap("name", Integer.toString(userId)),
            "name"
        );
    }

    SecurityMockMvcRequestPostProcessors.OAuth2LoginRequestPostProcessor githubLogin(Integer userId) {
        return oauth2Login().oauth2User(mockGithubUser(userId));
    }
}
