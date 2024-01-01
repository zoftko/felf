package com.zoftko.felf.models;

import com.zoftko.felf.entities.Installation;
import com.zoftko.felf.entities.Project;
import java.util.Optional;

public record ProjectData(
    String fullName,
    Optional<Project> project,
    Optional<Installation> installation,
    RepositoryInstallation repoInstall,
    Repository repository
) {
    public boolean isOwner(int userId) {
        if (repoInstall == null) {
            return false;
        }

        return installation
            .map(value -> value.getSender().equals(userId) && repoInstall.id().equals(value.getId()))
            .orElse(false);
    }

    public boolean hasPermissions() {
        if (repoInstall == null) {
            return false;
        }

        return (
            repoInstall.permissions().getOrDefault("pull_request", "").equals("write") &&
            repoInstall.suspendedAt() == null
        );
    }

    public Project initializeProject() throws IllegalStateException {
        if (installation.isEmpty() || repoInstall == null || repository == null) {
            throw new IllegalStateException();
        }

        var initProject = new Project();
        initProject.setFullName(fullName());
        initProject.setPrivate(repository.isPrivate());
        initProject.setInstallation(installation.get());
        initProject.setDefaultBranch(repository.defaultBranch());

        return initProject;
    }
}
