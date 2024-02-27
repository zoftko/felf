package com.zoftko.felf.db.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
public class Installation {

    @Id
    private Integer id;

    @Positive
    private Integer account;

    @Positive
    private Integer target;

    @NotBlank
    private String targetType;

    @NotBlank
    private String accountLogin;

    @Positive
    private Integer sender;

    public Installation(
        Integer id,
        Integer account,
        Integer target,
        String targetType,
        String accountLogin,
        Integer sender
    ) {
        this.id = id;
        this.account = account;
        this.target = target;
        this.targetType = targetType.trim();
        this.accountLogin = accountLogin.trim();
        this.sender = sender;
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
        this.targetType = type.trim();
    }

    public String getAccountLogin() {
        return accountLogin;
    }

    public void setAccountLogin(String accountLogin) {
        this.accountLogin = accountLogin.trim();
    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }
}
