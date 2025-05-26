package com.jefferyxhy.plugins.mavenprojectpeeker.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlTag;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.CacheService;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomCiManagement;
import org.jetbrains.idea.maven.dom.model.MavenDomIssueManagement;
import org.jetbrains.idea.maven.dom.model.MavenDomParent;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomProperties;
import org.jetbrains.idea.maven.dom.model.MavenDomScm;

import java.io.File;
import java.util.Collections;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenUtils {
    private static final String PROPERTY_REGEX = "\\$\\{([^}]+)\\}"; // The regex to match the property in the format of ${property}

    public static String getName(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "name", model -> model.getName().getValue());
    }

    public static String getDescription(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "description", model -> model.getDescription().getValue());
    }

    /**
     * Get the scm url of the maven project model
     * <p>
     * e.g.
     * <scm>
     * <connection>scm:git:ssh://git@github.com:atlassian/jackson-1.git</connection>
     * <developerConnection>scm:git:ssh://git@github.com:atlassian/jackson-1.git</developerConnection>
     * <url>https://github.com/atlassian/jackson-1</url>
     * </scm>
     */
    public static String getScmUrl(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "scm:url", model -> {
            MavenDomScm scm = model.getScm();
            return scm.exists() ? scm.getUrl().getValue() : null;
        });
    }

    public static String getScmConnection(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "scm:connection", model -> {
            MavenDomScm scm = model.getScm();
            return scm.exists() ? scm.getConnection().getValue() : null;
        });
    }

    public static String getScmDeveloperConnection(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "scm:developerConnection", model -> {
            MavenDomScm scm = model.getScm();
            return scm.exists() ? scm.getDeveloperConnection().getValue() : null;
        });
    }

    /**
     * Get the project connection management info
     * <p>
     * e.g.
     * <ciManagement>
     * <url>https://server-syd-bamboo.internal.atlassian.com/browse/DCNG-FILESTORE</url>
     * </ciManagement>
     *
     * @param project         the current project
     * @param domProjectModel the current project model
     * @return the ci management url
     * @throws MavenInvocationException if the maven invocation exception occurs
     */
    public static String getCiManagementUrl(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "ciManagement", model -> {
            MavenDomCiManagement ciManagement = model.getCiManagement();
            return ciManagement.exists() ? ciManagement.getUrl().getValue() : null;
        });
    }

    /**
     * Get the issue management info
     * <p>
     * e.g.
     * <issueManagement>
     * <system>IssueTracker</system>
     * <url>https://github.com/javaee/jax-ws-spec/issues</url>
     * </issueManagement>
     *
     * @param project         the current project
     * @param domProjectModel the current project model
     * @return the issue management url
     * @throws MavenInvocationException if the maven invocation exception occurs
     */
    public static String getIssueManagementUrl(Project project, MavenDomProjectModel domProjectModel) throws MavenInvocationException {
        return getMavenProjectModelData(project, domProjectModel, "issueManagement:url", model -> {
            MavenDomIssueManagement issueManagement = model.getIssueManagement();
            return issueManagement.exists() ? issueManagement.getUrl().getValue() : null;
        });
    }

    public static File getPomFile(String groupId, String artifactId, String version) throws MavenInvocationException {
        String groupPath = groupId.replace('.', File.separatorChar);

        String localRepoPath = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
        String pomFilePath = localRepoPath + File.separator + groupPath + File.separator + artifactId + File.separator + version + File.separator + artifactId + "-" + version + ".pom";
        File pomFile = new File(pomFilePath);

        if (!pomFile.exists()) {
            executeDependencyGet(groupId, artifactId, version);
        }

        return pomFile;
    }

    public static MavenDomProjectModel getDomProjectModel(Project project, File pomFile) {
        if (!pomFile.exists()) return null;

        VirtualFile virtualPomFile = LocalFileSystem.getInstance().findFileByIoFile(pomFile);
        return MavenDomUtil.getMavenDomProjectModel(project, virtualPomFile);
    }

    public static MavenDomProjectModel getParentDomProjectModel(Project project, MavenDomProjectModel childDomProjectModel) throws MavenInvocationException {
        MavenDomParent domParent = childDomProjectModel.getMavenParent();

        if (!domParent.exists()) return null;

        // Get parent artifact info
        String parentGroupId = domParent.getGroupId().getStringValue();
        String parentArtifactId = domParent.getArtifactId().getStringValue();
        String parentVersion = domParent.getVersion().getStringValue();

        // Get parent pom file and dom project model
        if (parentGroupId == null || parentArtifactId == null || parentVersion == null) return null;
        File pomFile = getPomFile(parentGroupId, parentArtifactId, parentVersion);
        return getDomProjectModel(project, pomFile);
    }

    public static String getCacheKeyOfProjectModel(MavenDomProjectModel domProjectModel) {
        return domProjectModel.getGroupId().getStringValue() + ":" + domProjectModel.getArtifactId().getStringValue() + ":" + domProjectModel.getVersion().getStringValue();
    }

    private static void executeDependencyGet(String groupId, String artifactId, String version) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Collections.singletonList("dependency:get"));
        request.setProperties(System.getProperties());
        request.getProperties().setProperty("artifact", groupId + ":" + artifactId + ":" + version);

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
    }

    /**
     * Get the data of the maven project model
     * <p>
     * This method will recursively get the data from the parent project model if the data is not found in the current project model
     *
     * @param project         the current project
     * @param domProjectModel the current project model
     * @param cacheKeySegment the cache key segment
     * @param modelExtractor  the function to extract the data from the project model
     * @return the target data from the project model
     * @throws MavenInvocationException if the maven invocation exception occurs
     */
    private static String getMavenProjectModelData(Project project, MavenDomProjectModel domProjectModel, String cacheKeySegment, Function<MavenDomProjectModel, String> modelExtractor) throws MavenInvocationException {
        if (domProjectModel == null) return null;

        String cacheKey = getCacheKeyOfProjectModel(domProjectModel) + ":" + cacheKeySegment;
        String value = CacheService.getInstance().get(cacheKey);

        if (!StringUtil.isEmpty(value)) return value;

        // Get the value from the current project model
        value = modelExtractor.apply(domProjectModel);

        // Get the value from the parent project model
        if (StringUtil.isEmpty(value)) {
            MavenDomProjectModel parentDomProjectModel = MavenUtils.getParentDomProjectModel(project, domProjectModel);
            value = getMavenProjectModelData(project, parentDomProjectModel, cacheKeySegment, modelExtractor);
        }

        // Replace the placeholders in the value and put it into the cache
        if (!StringUtil.isEmpty(value)) {
            value = replacePlaceHolders(value, domProjectModel);
            CacheService.getInstance().put(cacheKey, value);
        }

        return value;
    }

    private static String replacePlaceHolders(String target, MavenDomProjectModel domProjectModel) {
        if (target == null) return target;

        Matcher matcher = Pattern.compile(PROPERTY_REGEX).matcher(target);
        while (matcher.find()) {
            String placeHolderName = matcher.group(1);
            String placeHolerValue;

            // try to find the placeholder in the properties tag
            placeHolerValue = getPropertyValue(domProjectModel, placeHolderName);

            // try to find the placeholder in the nested tags
            if (placeHolerValue == null) {
                placeHolerValue = getNestedTagValue(domProjectModel.getXmlTag(), placeHolderName);
            }

            // replace the placeholder with the value
            if (placeHolerValue != null) {
                target = target.replaceAll(Pattern.quote(matcher.group()), placeHolerValue);
            }
        }

        return target;
    }

    private static String getPropertyValue(MavenDomProjectModel domProjectModel, String propertyName) {
        MavenDomProperties properties = domProjectModel.getProperties();
        XmlTag propertiesTag = properties.getXmlTag();

        if (propertiesTag == null) return null;

        for (XmlTag propertyTag : propertiesTag.getSubTags()) {
            if (propertyTag.getName().equals(propertyName)) {
                return propertyTag.getValue().getText();
            }
        }

        return null;
    }

    private static String getNestedTagValue(XmlTag rootTag, String chainedTagName) {
        if (rootTag == null) return null;

        String[] tagNames = chainedTagName.split("\\.");
        XmlTag currentTag = rootTag;
        for (int i = 0; i < tagNames.length; i++) {
            String tagName = tagNames[i];

            if (!currentTag.getName().equals(tagName)) {
                currentTag = currentTag.findFirstSubTag(tagName);
            }

            if (currentTag == null) return null;

            if (i == tagNames.length - 1) {
                return currentTag.getValue().getText();
            }
        }

        return null;
    }
}
