package at.tugraz.ist.akm.test.io.xml;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;

public class XmlReaderTest extends TestCase {

    public void test() {
        String xmlData = "<config><requestHandler pattern=\"/\" htmlFile=\"index.html\" class=\"myClass\"/></config>";

        XmlReader reader = new XmlReader(xmlData);

        List<XmlNode> nodes = reader.getNodes("requestHandler");
        Assert.assertEquals(1, nodes.size());

        Assert.assertEquals("/", nodes.get(0).getAttributeValue("pattern"));
        Assert.assertEquals("index.html", nodes.get(0).getAttributeValue("htmlFile"));
        Assert.assertEquals("myClass", nodes.get(0).getAttributeValue("class"));
    }
}