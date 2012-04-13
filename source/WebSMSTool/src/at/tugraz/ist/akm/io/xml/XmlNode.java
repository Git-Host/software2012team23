package at.tugraz.ist.akm.io.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class XmlNode {
    private final Node node;

    public XmlNode(final Node node) {
        this.node = node;
    }

    public String getValue() {
        return node.getNodeValue();
    }

    public String getName() {
        return node.getNodeName();
    }

    public String getAttributeValue(String attribute) {
        Node attributeNode = node.getAttributes().getNamedItem(attribute);
        return attributeNode != null ? attributeNode.getNodeValue() : null;
    }

    public List<XmlNode> getChildNodes(String name) {
        List<XmlNode> childNodes = new ArrayList<XmlNode>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeName().equals(name)) {
                childNodes.add(new XmlNode(childNode));
            }
        }
        return childNodes;
    }
}
