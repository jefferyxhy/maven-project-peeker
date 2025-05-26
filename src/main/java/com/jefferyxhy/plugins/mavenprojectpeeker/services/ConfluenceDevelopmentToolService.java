package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public final class ConfluenceDevelopmentToolService {
    private static final Logger log = Logger.getInstance(ConfluenceDevelopmentToolService.class);
    private static final String CONFLUENCE_DEVELOPMENT_TOOL_REMOTE_URL = "ssh://git@stash.atlassian.com:7997/confserver/confluence-development-tools.git";
    private static final String DEVELOPMENT_TOOL_DIRECTORY = "confluence-development-tools";
    private Project project;

    public static ConfluenceDevelopmentToolService getInstance() {
        return ApplicationManager.getApplication().getService(ConfluenceDevelopmentToolService.class);
    }

    public ConfluenceDevelopmentToolService withProject(Project project) {
        this.project = project;
        return this;
    }

    /**
     * Set up the development tool repository when repository is not valid for use:
     * 1. Set the default directory
     * 2. Pull the latest changes
     */
    public void setup() {
        // set the default directory and pull the latest changes
        try {
            if (!isValidDirectory(getDirectory())) {
                setDefaultDirectory();
                pull();
            }
        } catch (Exception ignored) {
        }
    }

    public void pull() throws InterruptedException, IOException {
        NotificationService.getInstance().notify("Git pull Confluence development tool repository: Started!");

        Process process = Runtime.getRuntime().exec("git pull", new String[]{}, new File(getDirectory()));
        process.waitFor(); // Wait for the process to complete

        systemOutPrintln(process.getInputStream());
        NotificationService.getInstance().notify("Git pull Confluence development tool repository: Completed!");
    }

    public void gitClone() throws IOException, InterruptedException {
        log.info("Git clone Confluence development tool repository: Started!");

        try {
            Path absolutePath = Paths.get(getDirectory());
            String folder = absolutePath.getFileName().toString();
            String directory = absolutePath.getParent().toString();

            String cloneCommand = String.format("git clone %s %s", CONFLUENCE_DEVELOPMENT_TOOL_REMOTE_URL, folder);
            Process process = Runtime.getRuntime().exec(cloneCommand, new String[]{}, new File(directory));
            process.waitFor(); // Wait for the process to complete

            systemOutPrintln(process.getInputStream());
            systemOutPrintln(process.getErrorStream());
            log.info("Git clone Confluence development tool repository: Completed!");
        } catch (Exception e) {
            log.error("Error clone repo: " + e.getMessage());
        }
    }

    /**
     * Get the path of repos folder under development tool repository
     *
     * @return the path of repos folder under development tool repository
     */
    public String getReposPath() {
        return Paths.get(getDirectory(), "repository", "repos").toString();
    }

    /**
     * Get the development tool repository directory
     *
     * @return the development tool repository directory path
     */
    public String getDirectory() {
        return StateStoreService.getInstance().getDevelopmentToolDirectory();
    }

    public void setDefaultDirectory() throws IOException, InterruptedException {
        File repoDir = PluginDirectoryService.createFile(DEVELOPMENT_TOOL_DIRECTORY);

        if (!repoDir.exists()) repoDir.mkdirs();

        setDirectory(repoDir.getAbsolutePath(), false);
    }

    public boolean setDirectory(String directory, boolean isInitialized) throws IOException, InterruptedException {
        if (isInitialized && !isValidDirectory(directory)) {
            NotificationService.getInstance().notify(project, "Invalid Confluence development tool repository directory, failed to connect to remote repository.", NotificationType.ERROR);
            return false;
        }

        StateStoreService.getInstance().withProject(project).setDevelopmentToolDirectory(directory);

        if (!isInitialized) gitClone();

        return true;
    }

    public boolean isValidDirectory(String developmentToolDirectory) {
        if (StringUtil.isEmpty(developmentToolDirectory)) return false;

        try {
            Process process = Runtime.getRuntime().exec("git config --get remote.origin.url", new String[]{}, new File(developmentToolDirectory));
            process.waitFor(); // Wait for the process to complete

            String lastOutputLine = systemOutPrintln(process.getInputStream());
            return CONFLUENCE_DEVELOPMENT_TOOL_REMOTE_URL.equals(lastOutputLine);
        } catch (Exception e) {
            log.info("[Maven Project Peeker] Invalid Confluence development tool repository directory, failed to connect to remote repository.");
            log.info(e.getMessage());
        }

        return false;
    }

    private String systemOutPrintln(InputStream inputStream) {
        String nextOutputLine = "";
        String nextNotNullLine = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((nextOutputLine = reader.readLine()) != null) {
                log.info(nextOutputLine);
                nextNotNullLine = nextOutputLine.trim();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return nextNotNullLine;
    }
}
