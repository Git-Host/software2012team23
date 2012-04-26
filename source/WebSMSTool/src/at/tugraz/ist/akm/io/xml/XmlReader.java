package at.tugraz.ist.akm.io.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;

public class XmlReader {
    private Document mDom = null;

    /**
     * 
     * @param data
     */
    public XmlReader(final String data) {
        read(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * 
     * @param context
     * @param filePath
     */
    public XmlReader(final Context context, final String filePath) {
        InputStream is = null;
        try {
            is = context.getAssets().open(filePath);
            read(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 
     * @param is
     */
    private void read(final InputStream is) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            mDom = builder.parse(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param name
     * @return
     */
    public List<XmlNode> getNodes(String name) {
        List<XmlNode> nodes = new ArrayList<XmlNode>();
        if(mDom == null) {
            return nodes;
        }
        Element root = mDom.getDocumentElement();

        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals(name)) {
                nodes.add(new XmlNode(node));
            }
        }
        return nodes;
    }
}