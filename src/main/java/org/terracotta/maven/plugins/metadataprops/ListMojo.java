package org.terracotta.maven.plugins.metadataprops;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

/**
 * @author Ludovic Orban
 */
@Mojo(name = "list")
public class ListMojo extends AbstractMetadataPropertiesMojo {

  @Parameter(defaultValue = "${project}")
  protected MavenProject activeProject;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Document document = parsePom();

      Map<String,String> properties = loadMetadataProperties(document);
      getLog().info("Found " + properties.size() + " metadata propert" + (properties.size() < 2 ? "y" : "ies"));
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        getLog().info(entry.getKey() + " = [" + entry.getValue() + "]");
      }

    } catch (Exception e) {
      throw new MojoExecutionException("Cannot list metadata properties from POM", e);
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

}
