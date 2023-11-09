package com.zoftko.felf.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Installation installation;

    @NotBlank
    private String fullName;

    private String defaultBranch;

    @NotBlank
    private String token;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Installation getInstallation() {
        return installation;
    }

    public void setInstallation(Installation installationId) {
        this.installation = installationId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName.trim();
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Update the project's token with a new one.
     * @param random Generator used to create the token.
     * @param encoder Used to hash the token.
     * @return The token before hashing takes place. This instance and the underlying database store the token's hash
     * and not the original token.
     */
    public String generateToken(SecureRandom random, PasswordEncoder encoder) {
        var bytes = new byte[32];
        random.nextBytes(bytes);

        var newToken = Base64.getEncoder().encodeToString(bytes);
        setToken(encoder.encode(newToken));

        return newToken;
    }
}
