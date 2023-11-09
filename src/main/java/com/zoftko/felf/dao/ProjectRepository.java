package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Optional<Project> findByFullName(String fullName);
}
