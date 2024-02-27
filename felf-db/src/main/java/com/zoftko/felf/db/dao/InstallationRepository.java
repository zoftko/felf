package com.zoftko.felf.db.dao;

import com.zoftko.felf.db.entities.Installation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationRepository extends JpaRepository<Installation, Integer> {
    Optional<Installation> findByAccountLogin(String accountLogin);
}
