package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Service
@State(name = "StateStoreService", storages = {@Storage("maven-project-peeker.xml")})
public final class StateStoreService implements PersistentStateComponent<StateStoreService.State> {
    private State state = new State();
    private Project project;

    public static StateStoreService getInstance() {
        return ApplicationManager.getApplication().getService(StateStoreService.class);
    }

    public StateStoreService withProject(Project project) {
        this.project = project;
        return this;
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getDevelopmentToolDirectory() {
        return state.developmentToolDirectory;
    }

    public void setDevelopmentToolDirectory(String developmentToolDirectory) {
        state.developmentToolDirectory = developmentToolDirectory;
        NotificationService.getInstance().notify(project, "Confluence development tool directory set: " + developmentToolDirectory);
    }

    static class State {
        public String developmentToolDirectory;
        public Map<String, String> data;
    }
}
