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