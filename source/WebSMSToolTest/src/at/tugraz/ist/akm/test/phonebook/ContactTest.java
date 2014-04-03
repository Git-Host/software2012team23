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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import android.net.Uri;
import at.tugraz.ist.akm.phonebook.Contact;

public class ContactTest extends TestCase
{

    public void test_cleanedNumber()
    {
        Contact.Number number = new Contact.Number(
                "0043699-123456789-1234567890", 1);
        Assert.assertEquals("00436991234567891234567890", number.getNumber());

        number = new Contact.Number("+436991234567890", 1);
        Assert.assertEquals("+436991234567890", number.getNumber());

        number = new Contact.Number("+43699-1234567890-1234567890", 1);
        Assert.assertEquals("+4369912345678901234567890", number.getNumber());
    }


    public void test_hashCodeOfDifferentButDefaultConstructedObjects()
    {
        Contact a = new Contact();
        Contact b = new Contact();
        assertEquals(a.hashCode(), b.hashCode());
    }


    public void test_hashCodeOfDifferentButEqualObjects()
    {
        Contact a = new Contact();
        Contact b = new Contact();
        List<Contact.Number> aPhones = new ArrayList<Contact.Number>();
        List<Contact.Number> bPhones = new ArrayList<Contact.Number>();

        aPhones.add(new Contact.Number("123", 1));
        bPhones.add(new Contact.Number("123", 1));
        aPhones.add(new Contact.Number("1234", 1));
        bPhones.add(new Contact.Number("1234", 1));

        a.setDisplayName("huaba");
        b.setDisplayName("huaba");
        a.setId(16);
        b.setId(16);
        a.setPhoneNumbers(aPhones);
        b.setPhoneNumbers(bPhones);
        a.setPhotoUri(null);
        b.setPhotoUri(null);
        a.setPhotoUri(Uri.parse("http://www.lala.com/"));
        b.setPhotoUri(Uri.parse("http://www.lala.com/"));
        a.setStarred(false);
        b.setStarred(false);

        assertEquals(a.hashCode(), b.hashCode());
    }

}
