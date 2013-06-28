# Properties Metadata Maven Plugin
This maven plugin allows the user to

## How to build :
Simply clone this repo and run mvn clean install

## Is is in Maven central ? :
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

    -DpreparationGoals=clean\ verify\ org.terracotta.maven.plugins:properties-metadata-maven-plugin:set -DcompletionGoals=org.terracotta.maven.plugins:properties-metadata-maven-plugin:clear -DcheckModificationExcludeList=pom.xml


## Authors :
This plugin was developed during Innovation Days by Terracotta, by

- [Ludovic Orban](https://github.com/lorban/)