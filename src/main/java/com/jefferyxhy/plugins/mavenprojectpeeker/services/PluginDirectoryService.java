package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;

import java.io.File;

@Service
public final class PluginDirectoryService {
    public static final String PLUGIN_DIRECTORY = "maven-project-peeker";

    public static PluginDirectoryService getInstance() {
        return ApplicationManager.getApplication().getService(PluginDirectoryService.class);
    }

    /**
     * Get the plugin data root directory
     * <p>
     * This directory is used to store plugin data
     *
     * @return File
     */
    public static File getPluginDataDirectory() {
        String configPath = PathManager.getConfigPath();
        File pluginDirectory = new File(configPath, PLUGIN_DIRECTORY);

        if (!pluginDirectory.exists()) {
            pluginDirectory.mkdirs();  // Create the directory if it doesn't exist
        }

        return pluginDirectory;
    }

    public static File createFile(String fileName) {
        return new File(getPluginDataDirectory(), fileName);
    }
}
