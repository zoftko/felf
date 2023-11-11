package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByInstallationId(Integer installationId);
}
