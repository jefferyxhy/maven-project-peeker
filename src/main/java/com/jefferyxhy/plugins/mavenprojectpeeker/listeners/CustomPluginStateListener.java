
package com.jefferyxhy.plugins.mavenprojectpeeker.listeners;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.diagnostic.Logger;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.ConfluenceDevelopmentToolService;
import org.jetbrains.annotations.NotNull;

public class CustomPluginStateListener implements PluginStateListener {
    private static final Logger log = Logger.getInstance(CustomPluginStateListener.class);
    public void install(@NotNull IdeaPluginDescriptor var1) {
        ConfluenceDevelopmentToolService.getInstance().setup();
    }
}
