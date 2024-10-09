package org.nagp.dataProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handler to read in XML data which may be used for test case inputs and expected values.
 *
 */
public class XmlParser {

  private static final Logger LOGGER = LogManager.getLogger(XmlParser.class);
  private String appNode = null;
  private Document currentDocument;
  private Document sourceDocument;

  public XmlParser(String host) {
    appNode = "APP_" + host;
    LOGGER.debug("App node   :  "  + appNode);
  }


  /** Method to parse XML data from a file based on a given xPath value.
   *
   * @param filePath The XML file to read data from
   * @param tagname A tag name in the xml file
   * @return Map containing the node data
   */
  @SuppressWarnings({"unchecked", "static-access", "rawtypes"})
  public Map parseXml(String filePath, String tagname) {
    HashMap result = new HashMap();

    // First we try to read data from the path
    InputStream fileStream = getClass().getResourceAsStream("/" + filePath);
    if (fileStream == null) {
      LOGGER.error("Problem reading the XML data file! Check to see if the file exists.");
    } else {
      try {
        DocumentBuilderFactory sourceFactory = DocumentBuilderFactory.newInstance();
        try {
          DocumentBuilder sourceBuilder = sourceFactory.newDocumentBuilder();
          sourceDocument = sourceBuilder.parse(fileStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
          LOGGER.error("Problem reading the XML data file!");
          e.printStackTrace();
        }

        // If we parsed the document OK, we now set the class currentDocument value
        sourceDocument = getDocumentWithValidNode(sourceDocument);
        currentDocument = sourceDocument;

        // Now we try to find the node matching the tag
        sourceDocument.getDocumentElement().normalize();

        // Get list of nodes matching a tag name.
        // Check each node in the list to see if it's an element node.
        // If it is then add that node to a temp list whose children are parsed further.
        LOGGER.info("Reading XML Elements at : " + tagname);
        NodeList resultNodeList = sourceDocument.getElementsByTagName(tagname);
        MyNodeList tempNodeList = new MyNodeList();

        String emptyNodeName = null;
        String emptyNodeValue = null;

        for (int index = 0; index < resultNodeList.getLength(); index++) {
          Node tempNode = resultNodeList.item(index);
          if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
            tempNodeList.addNode(tempNode);
          }
          emptyNodeName = tempNode.getNodeName();
          emptyNodeValue = tempNode.getNodeValue();
        }

        if (tempNodeList.getLength() == 0 && emptyNodeName != null && emptyNodeValue != null) {
          result.put(emptyNodeName, emptyNodeValue);
        } else {
          this.parseXmlNode(tempNodeList, result);
        }
      } finally {
        try {
          fileStream.close();
        } catch (IOException e) {
          LOGGER.debug("Error closing the stream.", e.getMessage());
        }
      }
    }
    return result;
  }

  /** Parse list of ELEMENT_NODE in the XML file to get data as map.
   *
    * @param nodeList List of nodes that are ELEMENT_NODE
   * @param result HashMap that stores the result of processing
   */
  @SuppressWarnings({"unchecked", "static-access", "rawtypes"})
  private void parseXmlNode(NodeList nodeList, HashMap result) {
    // Iterate through each node and build the map for all the child nodes and sub-nodes
    // including values and attributes
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()
          && node.getFirstChild() != null && node.getFirstChild().getNextSibling() != null
          || (node.getFirstChild() != null && node.getFirstChild().hasChildNodes())) {
        NodeList childNodes = node.getChildNodes();
        MyNodeList tempNodeList = new MyNodeList();
        for (int index = 0; index < childNodes.getLength(); index++) {
          Node tempNode = childNodes.item(index);
          if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
            tempNodeList.addNode(tempNode);
          }
        }
        HashMap counterHashMap = new HashMap();
        HashMap dataHashMap = new HashMap();
        if (result.containsKey(node.getNodeName())
            && ((HashMap) result.get(node.getNodeName())).containsKey(0)) {
          Map mapExisting = (Map) result.get(node.getNodeName());
          Integer index = 0;
          if (mapExisting.containsKey(0)) {
            while (true) {
              if (mapExisting.containsKey(index)) {
                counterHashMap.put(index, mapExisting.get(index));
                index++;
              } else {
                break;
              }
            }
          } else {
            result.put(node.getNodeName(), counterHashMap);
            counterHashMap.put("0", mapExisting);
            index = 1;
          }
          result.put(node.getNodeName(), counterHashMap);
          counterHashMap.put(index, dataHashMap);
        } else if (result.containsKey(node.getNodeName())) {
          counterHashMap.put(0, result.get(node.getNodeName()));
          result.put(node.getNodeName(), counterHashMap);
          counterHashMap.put(1,dataHashMap);
        } else {
          result.put(node.getNodeName(), dataHashMap);
        }
        if (node.getAttributes().getLength() > 0) {
          Map attributeMap = new HashMap();
          for (int attributeCtr = 0; attributeCtr < node.getAttributes().getLength();
              attributeCtr++) {
            attributeMap.put(node.getAttributes().item(attributeCtr).getNodeName(),
                node.getAttributes().item(attributeCtr).getNodeValue());
          }
          dataHashMap.put("__attributes", attributeMap);
        }
        this.parseXmlNode(tempNodeList, dataHashMap);
      } else if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()
          && node.getFirstChild() != null && node.getFirstChild().getNextSibling() != null) {
        this.putValue(result, node);
      } else if (node.getNodeType() == Node.ELEMENT_NODE) {
        this.putValue(result, node);
      }
    } // end for loop
  }

  /** Read in the value of a node and treat any Xpath style data as a reference.
   *
   * @param nodeValueXpath XPath value inside file
   * @return Data from the reference node
   */
  private Object getNodeValueFromXpathValue(String nodeValueXpath) {
    Object result;
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xpath = xPathFactory.newXPath();
    try {
      XPathExpression expr = xpath.compile(nodeValueXpath + "/text()");
      result = expr.evaluate(currentDocument, XPathConstants.STRING);
      LOGGER.debug("Evaluated expression: " + result);
      if (result == null) {
        result = "";
      }
    } catch (XPathExpressionException e) {
      LOGGER.debug(e.getMessage());
      LOGGER.debug("Error occurred evaluating the Xpath. Setting the value as blank.");
      result = "";
    }
    return result;
  }

  /** Method to set a node value first checking if the original value is a referenced
   * xPath value which can be expanded and set another value. This is useful to re-use
   * variables in the XML document in multiple places as needed.
   * @param result will be the original value or else an evaluated value if the value is an xpath
   * @param node Original node value to be checked.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void putValue(HashMap result, Node node) {
    HashMap attributeMap = new HashMap();
    Object nodeValue;
    if (node.getFirstChild() != null) {
      nodeValue = node.getFirstChild().getNodeValue();
      if (nodeValue != null) {
        nodeValue = nodeValue.toString().trim();
        if (nodeValue.toString().length() >= 2
            && nodeValue.toString().substring(0,2).equals("//")) {
          String nodeValueXpath = nodeValue.toString();
          LOGGER.debug("Node Value: " + nodeValueXpath);
          if (nodeValueXpath.contains("\\")) {
            nodeValueXpath = nodeValueXpath.replaceAll("\\\\", "/");
            LOGGER.debug("Found \\ in the node value. Replacing it with /.");
          }
          nodeValue = getNodeValueFromXpathValue(nodeValueXpath);
        }
      }
    } else {
      nodeValue = "";
    }
    HashMap nodeMap = new HashMap();
    nodeMap.put("value", nodeValue);
    Object putNode = nodeValue;
    if (node.getAttributes().getLength() > 0) {
      for (int attributeCtr = 0; attributeCtr < node.getAttributes().getLength(); attributeCtr++) {
        attributeMap.put(node.getAttributes().item(attributeCtr).getNodeName(),
            node.getAttributes().item(attributeCtr).getNodeValue());
      }
      nodeMap.put("__attributes", attributeMap);
      putNode = nodeMap;
    }
    HashMap counterHashMap = new HashMap();
    HashMap dataHashMap = new HashMap();
    if (result.containsKey(node.getNodeName()) && result.get(node.getNodeName()) instanceof HashMap
        && ((HashMap) result.get(node.getNodeName())).containsKey(0)) {
      Map mapExisting = (Map) result.get(node.getNodeName());
      Integer index = 0;
      if (mapExisting.containsKey(0)) {
        while (true) {
          if (mapExisting.containsKey(index)) {
            counterHashMap.put(index, mapExisting.get(index));
            index++;
          } else {
            break;
          }
        }
      } else {
        index = 1;
      }
      counterHashMap.put(index, putNode);
      result.put(node.getNodeName(), counterHashMap);
    } else if (result.containsKey(node.getNodeName())) {
      Object existingObject = result.get(node.getNodeName());
      result.put(node.getNodeName(),dataHashMap);
      dataHashMap.put(0,existingObject);
      dataHashMap.put(1,putNode);
    } else {
      result.put(node.getNodeName(), putNode);
    }
  }

  /** A helper method that will warn when the source XML document does not match the appNode.
   *
   * @param document Source document based on appNode value
   * @return Document based on appNode value
   */
  private Document getDocumentWithValidNode(Document document) /*throws Exception*/ {
    NodeList validNodeList = document.getElementsByTagName(appNode);
    if (validNodeList != null && validNodeList.getLength() > 0) {
      Node rootNode = validNodeList.item(0);
      NodeList childNodeList = rootNode.getParentNode().getChildNodes();
      for (int i = 0; i < childNodeList.getLength(); i++) {
        Node childNode = childNodeList.item(i);
        String childNodeName = childNode.getNodeName();
        if (! childNodeName.equalsIgnoreCase(appNode) && !childNodeName.startsWith("#")) {
          childNode.getParentNode().removeChild(childNode);
        }
      }
    } else {
      // TODO: if we need to throw error in case of invalid node in XML, uncomment
      // the below lines.
      //throw new Exception("Failed to retrieve the node ");
      LOGGER.warn("Failed to retrieve the node " + appNode + " from the XML."
          + " Using the existing node.");
    }
    return document;
  }

  /** A Customized nodelist class which implements a NodeList.
   *
   */
  static class MyNodeList implements NodeList {
    List<Node> nodes = new ArrayList<Node>();
    int length = 0;

    public MyNodeList() {

    }

    public void addNode(Node node) {
      nodes.add(node);
      length++;
    }

    @Override
    public Node item(int index) {
      try {
        return nodes.get(index);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    public int getLength() {
      return length;
    }
  }
}
