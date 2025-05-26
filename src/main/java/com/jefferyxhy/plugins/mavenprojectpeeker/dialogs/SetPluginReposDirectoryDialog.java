package com.jefferyxhy.plugins.mavenprojectpeeker.dialogs;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.ConfluenceDevelopmentToolService;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.PluginReposLoadService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class SetPluginReposDirectoryDialog extends DialogWrapper {
    private EditorTextField directoryField;
    private Project project;

    public SetPluginReposDirectoryDialog(Project project) {
        super(true);
        init();
        this.project = project;
        setTitle("Set Confluence development tool directory");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Add directory field
        String existingDirectory = ConfluenceDevelopmentToolService.getInstance().getDirectory();
        directoryField = new EditorTextField(existingDirectory != null ? existingDirectory : "Confluence development tool directory");
        directoryField.setPreferredSize(new Dimension(500, 100));
        directoryField.addMouseListener(createMouseListener());
        panel.add(directoryField, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void doOKAction() {
        try {
            super.doOKAction();
            updateDirectory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDirectory() throws IOException, InterruptedException {
        String updatedDirectory = directoryField.getText();

        if (updatedDirectory != null && ConfluenceDevelopmentToolService.getInstance().withProject(project).setDirectory(updatedDirectory, true)) {
            PluginReposLoadService.getInstance().withProject(project).load();
        }
    }

    private MouseListener createMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
                if (virtualFile != null) {
                    String path = virtualFile.getPath();
                    directoryField.setText(path);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
    }
}
