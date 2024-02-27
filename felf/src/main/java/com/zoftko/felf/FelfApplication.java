package com.zoftko.felf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackageClasses = { com.zoftko.felf.db.entities.Project.class })
@EnableJpaRepositories(basePackageClasses = { com.zoftko.felf.db.dao.ProjectRepository.class })
public class FelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(FelfApplication.class, args);
    }
}
