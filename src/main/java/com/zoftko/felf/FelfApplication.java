package com.zoftko.felf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(FelfApplication.class, args);
    }
}
