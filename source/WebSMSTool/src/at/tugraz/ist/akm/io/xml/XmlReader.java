/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.io.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import at.tugraz.ist.akm.trace.LogClient;

public class XmlReader {
    private Document mDom = null;
    private final static String mInputCharset = "UTF8";
    private LogClient mLog = new LogClient(this);

    /**
     * 
     * @param data
     */
    public XmlReader(final String data) {
        try
        {
            read(new ByteArrayInputStream(data.getBytes(mInputCharset)));
        } catch (UnsupportedEncodingException e)
        {
            mLog.debug("unsupported encoding: " + e.getMessage());
        }
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ParserConfigurationException parserConfigException) {
            parserConfigException.printStackTrace();
        } catch (SAXException saxException) {
            saxException.printStackTrace();
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