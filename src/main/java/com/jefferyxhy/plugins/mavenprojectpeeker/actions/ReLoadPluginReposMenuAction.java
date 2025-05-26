package com.jefferyxhy.plugins.mavenprojectpeeker.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.ConfluenceDevelopmentToolService;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.PluginReposLoadService;

public class ReLoadPluginReposMenuAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            ConfluenceDevelopmentToolService.getInstance().withProject(e.getProject()).pull();
            PluginReposLoadService.getInstance().withProject(e.getProject()).load();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
