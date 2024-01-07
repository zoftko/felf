package com.zoftko.felf.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    @Value("${felf.github.app.slug}")
    private String appSlug;

    @Value("${felf.app.name}")
    private String appName;

    @ModelAttribute("appSlug")
    public String appSlug() {
        return appSlug;
    }

    @ModelAttribute("appName")
    public String appName() {
        return appName;
    }
}
