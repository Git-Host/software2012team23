package at.tugraz.ist.akm.test.phonebook;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.tugraz.ist.akm.phonebook.Contact;

public class ContactTest extends TestCase{

    public void testCleanedNumber() {
    	Contact.Number number = new Contact.Number("0043699-123456789-1234567890", 1);
    	Assert.assertEquals("00436991234567891234567890", number.getCleanedUpNumber());
    	
    	number = new Contact.Number("+436991234567890", 1);
    	Assert.assertEquals("00436991234567890", number.getCleanedUpNumber());
    	
    	number = new Contact.Number("+43699-1234567890-1234567890",1);
    	Assert.assertEquals("004369912345678901234567890", number.getCleanedUpNumber());    	
    }
}