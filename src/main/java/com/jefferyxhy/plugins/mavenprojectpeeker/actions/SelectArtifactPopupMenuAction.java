package com.jefferyxhy.plugins.mavenprojectpeeker.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Artifact;
import com.jefferyxhy.plugins.mavenprojectpeeker.dialogs.DisplayArtifactInfoDialog;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.PluginArtifactMappingService;

public class SelectArtifactPopupMenuAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        String selectedText = editor.getSelectionModel().getSelectedText();
        try {
            Artifact artifact = PluginArtifactMappingService.getInstance().getArtifact(selectedText);
            new DisplayArtifactInfoDialog(e.getProject(), artifact).showAndGet();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
