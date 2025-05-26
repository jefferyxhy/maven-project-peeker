package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.jefferyxhy.plugins.mavenprojectpeeker.data.Repo;
import com.jefferyxhy.plugins.mavenprojectpeeker.utils.FileSearch;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public final class PluginReposArtifactsUpdateService {

    public static final String REPOS_CLONE_DIRECTORY = "/a/directory/to/store/cloned/plugin/repositories";

    private Project project;

    public static PluginReposArtifactsUpdateService getInstance() {
        return ApplicationManager.getApplication().getService(PluginReposArtifactsUpdateService.class);
    }

    public PluginReposArtifactsUpdateService withProject(Project project) {
        this.project = project;
        return this;
    }

    public void update() {
        Arrays.stream(new File(REPOS_CLONE_DIRECTORY).listFiles()).forEach(file -> updateArtifactsForOneRepo(file));
    }

    private void updateArtifactsForOneRepo(File repoDirectory) {
        Repo repo = PluginReposLoadService.getInstance().getRepos().stream().filter(r -> r.name.equals(repoDirectory.getName())).findFirst().orElse(null);

        if (repo == null) {
            return;
        }

        Map<String, String> artifacts = extractArtifactsOfRepo(repoDirectory);
        updateRepoJSFileWithArtifacts(repo, artifacts);
    }

    private void updateRepoJSFileWithArtifacts(Repo repo, Map<String, String> artifacts) {
        String developmentToolRepoFilePath = Paths.get(ConfluenceDevelopmentToolService.getInstance().getReposPath(), repo.name + ".js").toString();
        File developmentToolRepoFile = new File(developmentToolRepoFilePath);

        // Read existing JS file and insert artifacts information
        StringBuilder builder = new StringBuilder();
        String nextLine = "";
        try {
            Scanner myReader = new Scanner(developmentToolRepoFile);
            while (myReader.hasNextLine()) {
                nextLine = myReader.nextLine();
                builder.append(nextLine);

                if (nextLine.contains("]")) {
                    if (!nextLine.endsWith(",")) {
                        builder.append(",");
                    }
                    builder.append("artifacts: {" + artifacts.entrySet().stream().map(entry -> "'" + entry.getKey() + "': '" + entry.getValue() + "'").collect(Collectors.joining(", ")) + "}");
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
        }

        // Delete existing JS file
        new File(developmentToolRepoFilePath).delete();

        // Create new JS file and write update information
        File newRepoFile = new File(developmentToolRepoFilePath);
        try {
            FileWriter fileWriter = new FileWriter(newRepoFile, false);
            fileWriter.write(builder.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> extractArtifactsOfRepo(File repoDirectory) {
        Map<String, String> artifacts = new HashMap<>();

        Repo repo = PluginReposLoadService.getInstance().getRepos().stream().filter(r -> r.name.equals(repoDirectory.getName())).findFirst().orElse(null);

        if (repo == null) {
            return artifacts;
        }

        List<String> pomDirectories = new FileSearch().searchDirectory(repoDirectory, "pom.xml");
        pomDirectories.forEach(pomDirectory -> {
            try {
                // modify pom.xml with repository url
                String artifactId = getArtifactIdFromPomFile(new File(pomDirectory));

                String repoUrl = repo.url;
                if (repo.url.contains("stash.atlassian.com") && !repo.url.contains("/browse")) {
                    repoUrl = URI.create(repo.url).resolve("browse").toString();
                } else if (repo.url.contains("bitbucket.org") && !repo.url.contains("/src/master")) {
                    repoUrl = URI.create(repo.url).resolve("src/master").toString();
                }

                String artifactUrl = pomDirectory.replace(repoDirectory.getAbsolutePath(), repoUrl);
                artifacts.put(artifactId, artifactUrl);
            } catch (Exception e) {
            }
        });

        return artifacts;
    }

    private String getArtifactIdFromPomFile(File file) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(file));
        return model.getArtifactId();
    }
}
