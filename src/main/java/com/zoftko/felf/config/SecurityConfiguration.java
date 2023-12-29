package com.zoftko.felf.config;

import static com.zoftko.felf.controllers.StaticController.LOGIN_MAPPING;
import static org.springframework.security.config.Customizer.withDefaults;

import com.zoftko.felf.controllers.AnalysisController;
import com.zoftko.felf.controllers.WebhookController;
import com.zoftko.felf.security.WebhookAuthenticationToken;
import com.zoftko.felf.security.WebhookSecretFilter;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    public static final String AUTHORITY_OAUTH2_USER = "OAUTH2_USER";

    @Value("${felf.github.app.webhook.secret}")
    private String ghWebhookSecret;

    @Bean
    @Order(2)
    public SecurityFilterChain analysisFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(AnalysisController.MAPPING)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(WebhookController.MAPPING)
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().hasAuthority(WebhookAuthenticationToken.WEBHOOK_AUTHORITY)
            )
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(new WebhookSecretFilter(ghWebhookSecret), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers(LOGIN_MAPPING, "/static/**")
                    .permitAll()
                    .anyRequest()
                    .hasAuthority(AUTHORITY_OAUTH2_USER)
            )
            .csrf(withDefaults())
            .oauth2Login(oauth2 -> oauth2.loginPage(LOGIN_MAPPING).defaultSuccessUrl("/"))
            .logout(logout -> logout.logoutSuccessUrl(LOGIN_MAPPING));

        return http.build();
    }

    @Bean
    SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
