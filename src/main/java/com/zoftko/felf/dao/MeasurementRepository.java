package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Measurement;
import com.zoftko.felf.entities.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    @Query("SELECT m FROM Measurement m WHERE m.project = ?1 AND m.branch = ?2 ORDER BY m.createdAt DESC")
    Optional<Measurement> getLastMeasurementByBranch(Project project, String branch);
}
