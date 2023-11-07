package com.zoftko.felf.services;

import com.zoftko.felf.dao.InstallationRepository;
import com.zoftko.felf.entities.Installation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GithubService {

    public static final String HTTP_HEADER_GH_UID = "X-Github-Uid";
    public static final String QUALIFIER_INSTALL_TOKEN = "gh-install";
    public static final String QUALIFIER_APP_TOKEN = "gh-app";

    private final InstallationRepository installationRepository;

    @Autowired
    public GithubService(InstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
    }

    public List<Installation> getUserInstallations(Integer id) {
        return installationRepository.findBySender(id);
    }
}
