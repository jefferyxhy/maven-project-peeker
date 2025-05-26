package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Artifact;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Repo;

@Service
public final class PluginArtifactMappingService {
    public static PluginArtifactMappingService getInstance() {
        return ApplicationManager.getApplication().getService(PluginArtifactMappingService.class);
    }

    public Artifact getArtifact(String artifactId) {
        try {
            Repo matchedRepo = PluginReposLoadService.getInstance().getRepos().stream().filter(repo -> repo.artifacts.containsKey(artifactId)).findFirst().orElse(null);
            return new Artifact(artifactId, matchedRepo);
        } catch (Exception ignored) {
            return new Artifact(artifactId, null);
        }
    }
}
