package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Optional<Project> findByFullName(String fullName);
    Optional<Project> findByToken(String token);

    @Modifying
    @Query("UPDATE Project p SET p.defaultBranch = :branch WHERE p.fullName = :name")
    void updateDefaultBranchByFullName(@Param("name") String fullName, @Param("branch") String branch);

    @Modifying
    @Query("UPDATE Project p SET p.isPrivate = :private WHERE p.fullName = :name")
    void updateIsPrivateByFullName(@Param("name") String fullName, @Param("private") boolean isPrivate);
}
