package com.jefferyxhy.plugins.mavenprojectpeeker.data;

public class Artifact {
    private Repo repo;
    private String id;

    public Artifact(String id, Repo repo) {
        this.id = id;
        this.repo = repo;
    }

    public String getId() {
        return id;
    }

    public Repo getRepo() {
        return repo;
    }

    public String getUrl() {
        return repo == null ? null : repo.artifacts.get(id);
    }
}
