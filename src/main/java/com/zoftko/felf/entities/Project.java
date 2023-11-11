package com.zoftko.felf.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private Integer repository;

    @ManyToOne(fetch = FetchType.LAZY)
    private Installation installation;

    @NotBlank
    private String fullName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRepository() {
        return repository;
    }

    public void setRepository(Integer repositoryId) {
        this.repository = repositoryId;
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
}
