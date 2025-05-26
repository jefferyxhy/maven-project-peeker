package com.jefferyxhy.plugins.mavenprojectpeeker.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jefferyxhy.plugins.mavenprojectpeeker.dialogs.SetPluginReposDirectoryDialog;

public class SetPluginReposDirectoryAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        new SetPluginReposDirectoryDialog(e.getProject()).showAndGet();
    }
}
