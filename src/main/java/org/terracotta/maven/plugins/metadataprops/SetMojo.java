package org.terracotta.maven.plugins.metadataprops;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Ludovic Orban
 */
@Mojo(name = "set")
public class SetMojo extends AbstractMetadataPropertiesMojo {

  @Parameter(defaultValue = "${project}")
  protected MavenProject activeProject;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Document document = parsePom();
      clearMetadataProperties(document);
      Node propertiesNode = findOrAddPropertiesNode(document);

      String buildUrl = System.getenv("BUILD_URL"); // set by Jenkins
      String activeProfiles = buildActiveProfileList();
      String svnRevision = System.getenv("SVN_REVISION"); // set by Jenkins

      addBlankLineAfter(propertiesNode);
      addMetadataProperty(propertiesNode, "build.jenkins.url", buildUrl);
      addMetadataProperty(propertiesNode, "build.maven.active.profiles", activeProfiles);
      addMetadataProperty(propertiesNode, "svn.revision", svnRevision);

      writePom(document);
    } catch (Exception e) {
      throw new MojoExecutionException("Cannot add metadata properties to POM", e);
    }
  }

  private String buildActiveProfileList() {
    StringBuilder sb = new StringBuilder();
    List<Profile> activeProfiles = activeProject.getActiveProfiles();
    for (Profile activeProfile : activeProfiles) {
      sb.append(activeProfile.getId()).append(",");
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  @Override
  protected void addMetadataProperty(Node propertiesNode, String name, String value) {
    super.addMetadataProperty(propertiesNode, name, value);
    getLog().info("Setting " + name + " to [" + (value == null ? "" : value) + "]");
  }

}
