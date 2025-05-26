
package com.jefferyxhy.plugins.mavenprojectpeeker.listeners;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.diagnostic.Logger;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.ConfluenceDevelopmentToolService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomAppLifecycleListener implements AppLifecycleListener {
    private static final Logger log = Logger.getInstance(CustomAppLifecycleListener.class);
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {

        new Thread(() -> {
            ConfluenceDevelopmentToolService.getInstance().setup();
        }).start();
    }
}
