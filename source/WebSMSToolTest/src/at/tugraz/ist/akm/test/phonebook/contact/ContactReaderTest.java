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

package at.tugraz.ist.akm.test.phonebook.contact;

import java.util.List;

import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.ContactReader;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.test.testdata.PhonebookTestsHelper;

public class ContactReaderTest extends WebSMSToolActivityTestcase {

	private String[][] mTestContacts = null;

	public ContactReaderTest() {
		super(ContactReaderTest.class.getSimpleName());
		mTestContacts = new String[][] { { "First", "Last", "123" },
				{ "Senthon", "L", "12312323" }, { "Therock", "G", "0" },
				{ "Speedy", "R", "0" }, { "", "Baz", "0" }, { "Bar", "", "0" } };
	}

    public void testFetchContactsWithPhone() {
        try {
            PhonebookTestsHelper.storeContacts(mTestContacts, mContentResolver);
            ContactReader contactReader = new ContactReader(mContentResolver);

            logVerbose("get contacts with phone");
            ContactFilter filterWithPhone = new ContactFilter();
            filterWithPhone.setWithPhone(true);
            List<Contact> contacts = contactReader
                    .fetchContacts(filterWithPhone);
            PhonebookTestsHelper.logContacts(contacts);

            PhonebookTestsHelper.deleteContacts(mTestContacts, mContentResolver);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            assertTrue(false);
        }
    }
    
    public void testFetchContactsWithPhoneAndStarred() {
        try {
            PhonebookTestsHelper.storeContacts(mTestContacts, mContentResolver);
            ContactReader contactReader = new ContactReader(mContentResolver);


            logVerbose("get contacts with phone AND starred");
            ContactFilter filterWithPhoneAndStarred = new ContactFilter();
            filterWithPhoneAndStarred.setWithPhone(true);
            filterWithPhoneAndStarred.setIsStarred(true);
            List<Contact> contacts = contactReader.fetchContacts(filterWithPhoneAndStarred);
            PhonebookTestsHelper.logContacts(contacts);

            PhonebookTestsHelper.deleteContacts(mTestContacts, mContentResolver);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            assertTrue(false);
        }
    }
    
    public void testFetchContactsWithStar() {
        try {
            PhonebookTestsHelper.storeContacts(mTestContacts, mContentResolver);
            ContactReader contactReader = new ContactReader(mContentResolver);

            logVerbose("get starred contacts");
            ContactFilter filterStarred = new ContactFilter();
            filterStarred.setIsStarred(true);
            List<Contact> contacts = contactReader.fetchContacts(filterStarred);
            PhonebookTestsHelper.logContacts(contacts);
          
            PhonebookTestsHelper.deleteContacts(mTestContacts, mContentResolver);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            assertTrue(false);
        }
    }
    
    
    public void testFetchContacts() {
        try {
            PhonebookTestsHelper.storeContacts(mTestContacts, mContentResolver);
            ContactReader contactReader = new ContactReader(mContentResolver);

            logVerbose("get contacts unfiltered");
            ContactFilter noFilter = new ContactFilter();
            List<Contact> contacts = contactReader.fetchContacts(noFilter);
            PhonebookTestsHelper.logContacts(contacts);

            PhonebookTestsHelper.deleteContacts(mTestContacts, mContentResolver);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            assertTrue(false);
        }
    }

}
