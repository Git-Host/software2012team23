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

package at.tugraz.ist.akm.test.io.xml;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.io.xml.XmlReader;

public class XmlReaderTest extends TestCase {

    public void test() {
        String xmlData = "<config><requestHandler pattern=\"/\" dataFile=\"index.html\" class=\"myClass\"/></config>";

        XmlReader reader = new XmlReader(xmlData);

        List<XmlNode> nodes = reader.getNodes("requestHandler");
        Assert.assertEquals(1, nodes.size());

        Assert.assertEquals("/", nodes.get(0).getAttributeValue("pattern"));
        Assert.assertEquals("index.html", nodes.get(0).getAttributeValue("dataFile"));
        Assert.assertEquals("myClass", nodes.get(0).getAttributeValue("class"));
    }
}