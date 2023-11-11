package com.zoftko.felf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import java.util.List;

/**
 * Response returned by /installation/repositories
 */
public record InstallationRepos(@JsonProperty("total_count") Integer totalCount, List<Repo> repositories) {
    public record Repo(Integer id, String name, Boolean isPrivate, URL htmlUrl) {}
}
