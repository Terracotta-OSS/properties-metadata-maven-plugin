# Properties Metadata Maven Plugin
This maven plugin allows the user to add metadata to the pom, during release.

## Example :

Provided you launch the maven release plugin through Jenkins (or at least have the environment variables **BUILD_URL** and **SVN_REVISION** set), the released pom will have 3 new properties added to the properties tag :

```xml
<metadata.build.jenkins.url>http://jenkinsmaster.terracotta.lan:9000/job/forge-parent_trunk_releaser/9/</metadata.build.jenkins.url>
<metadata.build.maven.active.profiles>terracotta-repositories,release,tc-publish</metadata.build.maven.active.profiles>
<metadata.svn.revision>40501</metadata.svn.revision>
```

Those metadatas are used by the [Nexus Dependency Management Plugin] (https://github.com/Terracotta-OSS/nexus-dependency-management-plugin)

## How to build :
Simply clone this repo and run mvn clean install

## Is it in Maven central ? :
No, you will have to add Terracotta maven repositories to your pom.xml :

```xml
  <pluginRepositories>
    <pluginRepository>
      <id>terracotta-snapshots</id>
      <url>http://www.terracotta.org/download/reflector/snapshots</url>
    </pluginRepository>
    <pluginRepository>
      <id>terracotta-releases</id>
      <url>http://www.terracotta.org/download/reflector/releases</url>
    </pluginRepository>
  </pluginRepositories>
```


## How to use :
The plugin is org.terracotta.maven.plugins:properties-metadata-maven-plugin and it has two goals: set and clear.

You can try

    mvn org.terracotta.maven.plugins:properties-metadata-maven-plugin:set

and

    mvn org.terracotta.maven.plugins:properties-metadata-maven-plugin:clear

on the command line to get a feel of what modifications are going to be made to the pom.

The way it was designed to be used is to hook in to the release plugin's lifecyle by adding those properties to the Release Plugin's config (look for "Release goals and options" and "DryRun goals and options" in Jenkins):

    -DpreparationGoals="clean verify org.terracotta.maven.plugins:properties-metadata-maven-plugin:set" -DcompletionGoals="org.terracotta.maven.plugins:properties-metadata-maven-plugin:clear" -DcheckModificationExcludeList=pom.xml

You can read the [maven release plugin documentation for more info on the preparationGoals and completionGoals you can hook to during release:prepare ] (http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html)


## Authors :
This plugin was developed during Innovation Days by Terracotta, by

- [Ludovic Orban](https://github.com/lorban/)