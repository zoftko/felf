package com.zoftko.felf.dao;

import com.zoftko.felf.entities.Installation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationRepository extends JpaRepository<Installation, Integer> {
    List<Installation> findBySender(Integer sender);
}
