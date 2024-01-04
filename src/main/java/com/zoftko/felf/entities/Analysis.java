package com.zoftko.felf.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
public class Analysis {

    public enum CommentStatus {
        NOOP,
        TODO,
        FAIL,
    }

    public Analysis() {
        comment = CommentStatus.NOOP;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    private LocalDateTime createdAt;

    @NotBlank
    private String ref;

    @Min(40)
    @Max(40)
    @Pattern(regexp = "^[0-9A-Za-z]{40}$")
    @Column(columnDefinition = "char(40)")
    private String sha;

    @Embedded
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "char(4)")
    private CommentStatus comment;

    private Long commentId;

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

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public CommentStatus getComment() {
        return comment;
    }

    public void setComment(CommentStatus comment) {
        this.comment = comment;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
}
