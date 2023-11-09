package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Installation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationRepository extends JpaRepository<Installation, Integer> {
    Optional<Installation> findByAccountLogin(String accountLogin);
}
