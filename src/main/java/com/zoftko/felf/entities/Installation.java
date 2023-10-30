package com.zoftko.felf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Installation {

    @Id
    private Integer id;

    @NotNull
    private Integer account;

    @NotNull
    private Integer target;

    @NotBlank
    @Column(length = 24, columnDefinition = "char")
    private String targetType;

    public Installation(Integer id, Integer account, Integer target, String targetType) {
        this.id = id;
        this.account = account;
        this.target = target;
        this.targetType = targetType;
    }

    public Installation() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String type) {
        this.targetType = type;
    }
}
