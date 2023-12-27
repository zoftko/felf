package com.zoftko.felf.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    private LocalDateTime createdAt;

    @NotBlank
    private String ref;

    @NotBlank
    @Column(columnDefinition = "char(40)")
    private String sha;

    private Integer textSize;

    private Integer bssSize;

    private Integer dataSize;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project projectId) {
        this.project = projectId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime time) {
        this.createdAt = time;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Integer getTextSize() {
        return textSize;
    }

    public void setTextSize(Integer textSize) {
        this.textSize = textSize;
    }

    public Integer getBssSize() {
        return bssSize;
    }

    public void setBssSize(Integer bssSize) {
        this.bssSize = bssSize;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }
}
