package com.jefferyxhy.plugins.mavenprojectpeeker.providers;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import com.jefferyxhy.plugins.mavenprojectpeeker.utils.MavenUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.intellij.lang.documentation.DocumentationMarkup.CONTENT_END;
import static com.intellij.lang.documentation.DocumentationMarkup.CONTENT_START;
import static com.intellij.lang.documentation.DocumentationMarkup.GRAYED_END;
import static com.intellij.lang.documentation.DocumentationMarkup.GRAYED_START;

public class MavenProjectDocumentationProvider extends AbstractDocumentationProvider {
    private static final String POM_EXTENSION = ".pom";
    private static final String POM_FILE = "pom.xml";
    private static final String TAG_BR = "<br>";
    private static final String TAG_HR = "<hr>";
    private static final String HTML_BODY_START = "<html><body style='white-space: nowrap;'>";
    private static final String HTML_BODY_END = "</body></html>";

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement context) {
        return null;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement context) {
        if (!(element instanceof XmlFile xmlFile) || !(xmlFile.getName().endsWith(POM_EXTENSION) || xmlFile.getName().endsWith(POM_FILE)))
            return null;

        StringBuilder docBuilder = new StringBuilder();
        MavenDomProjectModel domProjectModel = getDomProjectModel((XmlFile) element);

        docBuilder.append(HTML_BODY_START).append(CONTENT_START);

        // Name
        appendDocumentation(docBuilder, () -> getDocumentationForName(element.getProject(), domProjectModel));

        // Description
        appendDocumentation(docBuilder, () -> getDocumentationForDescription(element.getProject(), domProjectModel));

        docBuilder.append(TAG_HR);

        // SCM
        appendDocumentation(docBuilder, () -> getDocumentationForScm(element.getProject(), domProjectModel));

        // CI management
        appendDocumentation(docBuilder, () -> getDocumentationForCiManagement(element.getProject(), domProjectModel));

        // Issue management
        appendDocumentation(docBuilder, () -> getDocumentationForIssueManagement(element.getProject(), domProjectModel));

        docBuilder.append(TAG_HR);

        // File link
        docBuilder.append(asRenderedDoc("Pom path", asFileLink((XmlFile) element), false)).append(TAG_HR);

        docBuilder.append(CONTENT_END).append(HTML_BODY_END);

        return docBuilder.toString();
    }

    private void appendDocumentation(StringBuilder docBuilder, Callable<String> callable) {
        try {
            String documentation = callable.call();
            if (documentation != null) docBuilder.append(documentation);
        } catch (Exception ignored) {
        }
    }

    private String getDocumentationForScm(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        List<String> docs = new ArrayList<>();

        // Scm url
        String scmUrl = MavenUtils.getScmUrl(project, domProjectModel);
        if (scmUrl != null) docs.add(asRenderedDoc("Repo Url", scmUrl, true));

        // Scm connection
        String scmConnection = MavenUtils.getScmConnection(project, domProjectModel);
        if (scmConnection == null) scmConnection = MavenUtils.getScmDeveloperConnection(project, domProjectModel);
        if (scmConnection != null) docs.add(asRenderedDoc("Repo Connection", scmConnection, true));

        return docs.isEmpty() ? null : String.join("", docs);
    }

    private String getDocumentationForName(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        String name = MavenUtils.getName(project, domProjectModel);
        return name != null ? name + TAG_BR + TAG_BR : null;
    }

    private String getDocumentationForDescription(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        String description = MavenUtils.getDescription(project, domProjectModel);
        return description != null ? description + TAG_BR : null;
    }

    private String getDocumentationForCiManagement(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        String ciManagementUrl = MavenUtils.getCiManagementUrl(project, domProjectModel);
        return ciManagementUrl != null ? asRenderedDoc("CI Management", ciManagementUrl, true) : null;
    }

    private String getDocumentationForIssueManagement(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        String issueManagementUrl = MavenUtils.getIssueManagementUrl(project, domProjectModel);
        return issueManagementUrl != null ? asRenderedDoc("Issue Management", issueManagementUrl, true) : null;
    }

    private String asRenderedDoc(String definition, String content, boolean isLink) {
        content = isLink ? asLink(content, content) : content;
        return GRAYED_START + definition + " : " + GRAYED_END + content + TAG_BR;
    }

    private String asLink(String url, String text) {
        return String.format("<a href=\"%s\">%s</a>", url, text);
    }

    private String asFileLink(XmlFile xmlFile) {
        String filePath = xmlFile.getVirtualFile().getPath();
        String fileUrl = xmlFile.getVirtualFile().getUrl();

        return asLink(fileUrl, filePath);
    }

    private MavenDomProjectModel getDomProjectModel(XmlFile element) {
        return DomManager.getDomManager(element.getProject()).getFileElement(element, MavenDomProjectModel.class).getRootElement();
    }
}
