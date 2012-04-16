package at.tugraz.ist.akm.test.webservice.protocol.json;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;

public class JsonFactoryTest extends TestCase {
    private final Logable log = new Logable(getClass().getSimpleName());

    public void test() {
        JsonFactory factory = new JsonFactory();
        Contact contact = new Contact();
        contact.setDisplayName("therock");
        contact.setName("Thomas");
        contact.setFamilyName("Gödl");

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
        log.v("created object: " + createdObject.toString());
    }

}
