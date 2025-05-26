package com.jefferyxhy.plugins.mavenprojectpeeker.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.PluginReposArtifactsUpdateService;
import org.jetbrains.annotations.NotNull;

/**
 * a script kind of functionality to help read through all confluence repositories
 * and add artifacts information to each js file under development tool repos directory
 */
public class UpdatePluginReposArtifactsMenuAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(false); // Disable this functionality as it should be used during development
        e.getPresentation().setVisible(false);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PluginReposArtifactsUpdateService.getInstance().update();
    }

    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
