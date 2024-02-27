package com.zoftko.felf.db.dao;

import com.zoftko.felf.db.entities.Analysis;
import com.zoftko.felf.db.entities.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    @Query("SELECT m FROM Analysis m WHERE m.project = ?1 AND m.ref = ?2 ORDER BY m.createdAt DESC")
    Optional<Analysis> getLastAnalysisByRef(Project project, String ref);

    Optional<Analysis> findByProjectAndRef(Project project, String ref);
    List<Analysis> findByCommentOrderByCreatedAtAsc(Analysis.CommentStatus status, Pageable page);
}
