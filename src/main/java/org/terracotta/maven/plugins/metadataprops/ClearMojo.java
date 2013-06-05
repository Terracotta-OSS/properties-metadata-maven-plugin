package org.terracotta.maven.plugins.metadataprops;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.w3c.dom.Document;

/**
 * @author Ludovic Orban
 */
@Mojo(name = "clear")
public class ClearMojo extends AbstractMetadataPropertiesMojo {
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Document document = parsePom();

      int count = clearMetadataProperties(document);

      if (count > 0) {
        writePom(document);
      }
      getLog().info("cleared " + count + " metadata propert" + (count < 2 ? "y" : "ies"));
    } catch (Exception e) {
      throw new MojoExecutionException("Cannot clear metadata properties from POM", e);
    }
  }
}
