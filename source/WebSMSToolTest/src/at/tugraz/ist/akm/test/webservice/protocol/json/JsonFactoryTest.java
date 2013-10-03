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

package at.tugraz.ist.akm.test.webservice.protocol.json;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonFactoryTest extends TestCase {
    private final LogClient log = new LogClient(this);

    public void test() {
        JsonFactory factory = new JsonFactory();
        Contact contact = new Contact();
        contact.setDisplayName("therock");
        contact.setName("Thomas");
        contact.setFamilyName("Goedl");

        List<Contact.Number> phoneNumbers = new ArrayList<Contact.Number>();

        Contact.Number number = new Contact.Number("1234", 1);
        phoneNumbers.add(number);
        number = new Contact.Number("1235", 1);
        phoneNumbers.add(number);
        number = new Contact.Number("1236", 1);
        phoneNumbers.add(number);
        number = new Contact.Number("1237", 1);
        phoneNumbers.add(number);

        contact.setPhoneNumbers(phoneNumbers);

        JSONObject createdObject = factory.createJsonObject(contact);
        log.logVerbose("created object: " + createdObject.toString());
        
        //TODO: add ASSERT statement
    }

}
