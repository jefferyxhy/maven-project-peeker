package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.google.gson.Gson;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Repo;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.util.Objects.requireNonNullElse;

@Service
public final class PluginReposLoadService {
    private Project project;
    private List<Repo> repos = new ArrayList<>();

    public static PluginReposLoadService getInstance() {
        return ApplicationManager.getApplication().getService(PluginReposLoadService.class);
    }

    public PluginReposLoadService() {
        load();
    }

    public void load() {
        loadRepos();
    }

    public List<Repo> getRepos() {
        return repos;
    }

    public PluginReposLoadService withProject(Project project) {
        this.project = project;
        return this;
    }

    private void loadRepos() {
        String reposDirectory = ConfluenceDevelopmentToolService.getInstance().getReposPath();

        if (StringUtil.isEmpty(reposDirectory)) {
            NotificationService.getInstance().notify(project, "Please set plugin repos directory before load data!", NotificationType.ERROR);
            return;
        } else {
            NotificationService.getInstance().notify(project, "Load plugin repos data: Started!");
        }

        repos.clear();
        File directoryFile = new File(reposDirectory);
        Arrays.stream(requireNonNullElse(directoryFile.listFiles(), new File[0]))
                .filter(fileEntry -> fileEntry.getName().endsWith(".js"))
                .forEach(fileEntry -> loadRepo(new File(fileEntry.getPath())));

        NotificationService.getInstance().notify(project, "Load plugin repos data: Completed!");
    }

    private void loadRepo(File file) {
        String repoInfoString = readRepoJSFile(file);

        try {
            // Stringify JavaScript object into JSON format
            // built in JavaScript engine is deprecated in JDK 11 and removed from JDK 15, so use standalone nashorn engine as replacement
            ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();

            engine.eval(String.format("Object.bindProperties(this, %s);", repoInfoString));
            repoInfoString = (String) engine.eval("JSON.stringify(this);");
        } catch (Exception e) {
            NotificationService.getInstance().notify(project, "Can not parse plugin repo info: " + file.getName() + " JDK version: " + System.getProperty("java.version"), NotificationType.ERROR);
            NotificationService.getInstance().notify(project, e.getMessage(), NotificationType.ERROR);
        }

        try {
            // Parse JSON into Java HashMap
            Gson gson = new Gson();
            Repo repo = gson.fromJson(repoInfoString, Repo.class);

            // Store repo information
            repos.add(repo);
        } catch (Exception e) {
            NotificationService.getInstance().notify(project, "--------- Load repo: gson parse error for " + file.getName(), NotificationType.ERROR);
            NotificationService.getInstance().notify(project, e.getMessage(), NotificationType.ERROR);
        }
    }

    public String readRepoJSFile(File file) {
        // It only returns the content between {}
        StringBuilder builder = new StringBuilder();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                builder.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            NotificationService.getInstance().notify(project, "Can not read plugin repo file: " + file.getName(), NotificationType.ERROR);
        }

        String content = builder.toString();
        return content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
    }
}
