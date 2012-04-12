package at.tugraz.ist.akm.io.xml;

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
        return node.getAttributes().getNamedItem(attribute).getNodeValue();
    }
}
