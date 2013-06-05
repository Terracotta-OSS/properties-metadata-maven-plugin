package org.terracotta.maven.plugins.metadataprops;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author Ludovic Orban
 */
public abstract class AbstractMetadataPropertiesMojo extends AbstractMojo {

  private static final List<String> PRECEDING_NODE_NAMES =
      Arrays.asList("modelVersion", "parent", "groupId", "artifactId", "version", "packaging", "name", "description");

  private static final String LINE_SEP = System.getProperty("line.separator");
  private static final String METADATA_PREFIX = "metadata.";


  @Component
  private MavenProject rootProject;


  protected void addBlankLineAfter(Node node) {
    Document document = node.getOwnerDocument();
    String indentation = guessIndentation(document);
    node.appendChild(document.createTextNode(LINE_SEP + indentation));
  }

  protected int clearMetadataProperties(Document document) {
    int count = 0;
    Node propertiesNode = quickXPath(document, "/properties");
    if (propertiesNode != null) {
      List<Node> toRemove = new ArrayList<Node>();

      NodeList childNodes = propertiesNode.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().startsWith(METADATA_PREFIX)) {
          toRemove.add(node);

          if (i > 0) {
            Node previousNode = node.getParentNode().getChildNodes().item(i - 1);
            if (previousNode.getNodeType() == Node.TEXT_NODE) {
              toRemove.add(previousNode);
            }
          }
          count++;
        }
      }

      for (Node node : toRemove) {
        propertiesNode.removeChild(node);
      }
    }
    return count;
  }

  protected Map<String, String> loadMetadataProperties(Document document) {
    Map<String, String> properties = new HashMap<String, String>();

    Node propertiesNode = quickXPath(document, "/properties");
    if (propertiesNode != null) {
      NodeList childNodes = propertiesNode.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().startsWith(METADATA_PREFIX)) {
          String name = node.getNodeName().substring(METADATA_PREFIX.length());
          String value = node.getTextContent();
          properties.put(name, value);
        }
      }
    }

    return properties;
  }

  protected Node findOrAddPropertiesNode(Document document) {
    Node propertiesNode = quickXPath(document, "/properties");
    if (propertiesNode == null) {
      Node insertBeforeThisNode = null;

      NodeList childNodes = document.getDocumentElement().getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE && !PRECEDING_NODE_NAMES.contains(node.getNodeName())) {
          insertBeforeThisNode = node;
          break;
        }
      }

      propertiesNode = document.createElement("properties");

      String indentation = guessIndentation(document);
      if (insertBeforeThisNode == null) {
        document.getDocumentElement().appendChild(document.createTextNode(LINE_SEP));
        document.getDocumentElement().appendChild(document.createTextNode(indentation));
        document.getDocumentElement().appendChild(propertiesNode);
        document.getDocumentElement().appendChild(document.createTextNode(LINE_SEP));
      } else {
        Node indentNode = document.createTextNode(indentation);
        document.getDocumentElement().insertBefore(indentNode, insertBeforeThisNode);
        Node newLineNode2 = document.createTextNode(LINE_SEP + LINE_SEP);
        document.getDocumentElement().insertBefore(newLineNode2, indentNode);
        document.getDocumentElement().insertBefore(propertiesNode, newLineNode2);
      }
    }
    return propertiesNode;
  }

  protected void addMetadataProperty(Node propertiesNode, String name, String value) {
    String indentation = guessIndentation(propertiesNode.getOwnerDocument());

    Document document = propertiesNode.getOwnerDocument();
    Element buildUrlElement = document.createElement(METADATA_PREFIX + name);
    buildUrlElement.appendChild(document.createTextNode(value == null ? "" : value));
    propertiesNode.appendChild(document.createTextNode(indentation));
    propertiesNode.appendChild(buildUrlElement);
    propertiesNode.appendChild(document.createTextNode(LINE_SEP + indentation));
  }

  private String guessIndentation(Document document) {
    Element documentElement = document.getDocumentElement();
    Node item = documentElement.getChildNodes().item(0);
    if (item.getNodeType() == Node.TEXT_NODE) {
      Text text = (Text)item;
      return text.getTextContent().replace("\n", "").replace("\r", "");
    }
    return "    ";
  }

  protected Document parsePom() throws ParserConfigurationException, IOException, SAXException {
    File pomFile = rootProject.getFile();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    return dBuilder.parse(pomFile);
  }

  protected void writePom(Document document) throws TransformerException {
    File pomFile = rootProject.getFile();
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(pomFile);
    transformer.transform(source, result);
  }

  protected Node quickXPath(Document doc, String xpath) {
    if (xpath.startsWith("/")) {
      xpath = xpath.substring(1);
    }
    String[] pathes = xpath.split("/");

    Node node = findChildNode(doc.getDocumentElement(), pathes[0]);
    if (node == null) {
      return null;
    }
    for (int i = 1; i < pathes.length; i++) {
      String nodeName = pathes[i];
      node = findChildNode(node, nodeName);
    }

    return node;
  }

  private static Node findChildNode(Node parentNode, String childNodeName) {
    if (parentNode == null) {
      return null;
    }

    NodeList childNodes = parentNode.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeName().equals(childNodeName)) {
        return node;
      }
    }
    return null;
  }

}
