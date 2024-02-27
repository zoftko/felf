package com.zoftko.fakelf;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
@EntityScan(basePackageClasses = { com.zoftko.felf.db.entities.Project.class })
@EnableJpaRepositories(basePackageClasses = { com.zoftko.felf.db.dao.ProjectRepository.class })
public class FakelfApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FakelfApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
