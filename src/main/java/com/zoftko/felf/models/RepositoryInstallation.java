package com.zoftko.felf.models;

import java.time.LocalDateTime;
import java.util.Map;

public record RepositoryInstallation(
    Integer id,
    Map<String, String> permissions,
    LocalDateTime suspendedAt
) {}
