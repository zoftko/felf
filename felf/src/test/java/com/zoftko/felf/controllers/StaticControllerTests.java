package com.zoftko.felf.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = StaticController.class)
class StaticControllerTests extends BaseControllerTest {

    @Test
    void loginSignupPage() throws Exception {
        mockMvc
            .perform(get("/login?signup=true"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No signup required")));
    }

    @Test
    void loginPage() throws Exception {
        mockMvc
            .perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome back")));
    }
}
