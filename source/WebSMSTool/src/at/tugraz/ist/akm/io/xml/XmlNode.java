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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class XmlNode
{
    private final Node mNode;


    public XmlNode(final Node node)
    {
        this.mNode = node;
    }


    public String getValue()
    {
        return mNode.getNodeValue();
    }


    public String getName()
    {
        return mNode.getNodeName();
    }


    public String getAttributeValue(String attribute)
    {
        Node attributeNode = mNode.getAttributes().getNamedItem(attribute);
        return attributeNode != null ? attributeNode.getNodeValue() : null;
    }


    public List<XmlNode> getChildNodes(String name)
    {
        List<XmlNode> childNodes = new ArrayList<XmlNode>();
        for (int idx = 0; idx < mNode.getChildNodes().getLength(); idx++)
        {
            Node childNode = mNode.getChildNodes().item(idx);
            if (childNode.getNodeName().equals(name))
            {
                childNodes.add(new XmlNode(childNode));
            }
        }
        return childNodes;
    }
}
