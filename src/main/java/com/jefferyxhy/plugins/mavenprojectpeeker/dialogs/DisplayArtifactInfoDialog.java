package com.jefferyxhy.plugins.mavenprojectpeeker.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Artifact;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Repo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class DisplayArtifactInfoDialog extends DialogWrapper {

    private Artifact artifact;
    private Project project;
    private Repo repo;

    public DisplayArtifactInfoDialog(Project project, Artifact artifact) {
        super(true);

        this.project = project;
        this.artifact = artifact;
        this.repo = artifact.getRepo();

        init();
        setTitle("Confluence artifact information");
        setOKButtonText("Goto Bitbucket");
        setOKActionEnabled(repo != null);
    }

    @Override
    public void doOKAction() {
        try {
            super.doOKAction();
            gotoArtifactBitbucketPage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void gotoArtifactBitbucketPage() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(artifact.getUrl()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel;

        if (repo == null) {
            panel = new JPanel(new FlowLayout());
            panel.add(new Label("No Confluence developed artifact found for: " + artifact.getId()), BorderLayout.CENTER);
        } else {
            panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(3, 3, 3, 6);

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new Label("Artifact ID"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(new Label(artifact.getId()), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new Label("Artifact URL"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            panel.add(new Label(artifact.getUrl()), gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new Label("Repository"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            panel.add(new Label(repo.name), gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new Label("Repo Connection"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 3;
            panel.add(new Label(repo.connection), gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(new Label("Repo URL"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 4;
            panel.add(new Label(repo.url), gbc);
        }
        return panel;
    }
}
