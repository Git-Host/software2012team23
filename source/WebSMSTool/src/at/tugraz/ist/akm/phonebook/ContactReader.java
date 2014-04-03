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

package at.tugraz.ist.akm.phonebook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.ContactQueryBuilder;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.trace.LogClient;

public class ContactReader
{

    private ContentResolver mContentResolver = null;
    private LogClient mLog = new LogClient(this);


    public ContactReader(ContentResolver contentResolver)
    {
        mContentResolver = contentResolver;
    }


    public List<Contact> fetchContacts(ContactFilter filter)
    {

        long startTimestamp = System.currentTimeMillis();

        Cursor people = queryContacts(filter);
        List<Contact> contacts = null;
        long fetchTimestamp = System.currentTimeMillis();

        try
        {
            if (people != null)
            {
                contacts = new Vector<Contact>(people.getCount());
                while (people.moveToNext())
                {
                    contacts.add(parseToContact(people));
                }
                people.close();
            }
        }
        catch (Exception ex)
        {
            mLog.error("can not get cursor to contacts", ex);
        }
        long parseTimestamp = System.currentTimeMillis();

        mLog.debug("fetching took " + (fetchTimestamp - startTimestamp) + " ms");
        mLog.debug("parsing " + contacts.size() + " contacts took "
                + (parseTimestamp - startTimestamp) + " ms");

        return contacts;
    }


    private Cursor queryContacts(ContactFilter filter)
    {
        ContactQueryBuilder qBuild = new ContactQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = qBuild.getQueryArgs();
        return mContentResolver.query(queryParameters.uri, queryParameters.as,
                queryParameters.where, queryParameters.like,
                queryParameters.sortBy);
    }

    private Contact parseToContact(Cursor person)
    {

        Contact contact = new Contact();

        String contactId = person.getString(person
                .getColumnIndex(ContactsContract.Contacts._ID));
        String displayName = person.getString(person
                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        boolean starred = (1 == Integer.parseInt(person.getString(person
                .getColumnIndex(ContactsContract.Contacts.STARRED))));

        contact.setDisplayName(displayName);
        contact.setId(Integer.parseInt(contactId));
        contact.setStarred(starred);
        // all durations @ ~160 contacts
        // { total duration 6.3sec

        collectPhotoData(contact, Long.parseLong(contactId));

        // }
        // { total duration 3.4sec
        collectPhoneNumberDetails(contact, contactId);
        // }

        mLog.debug("parsed contact [" + contact.getDisplayName()
                + "] with id [" + contact.getId() + "]");
        return contact;
    }


    private void collectPhoneNumberDetails(Contact contact, String contactId)
    {
        String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                + " = ?";
        String[] as = { ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE };
        String[] like = { contactId };
        Cursor phoneNumbers = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, as, where,
                like, null);

        if (phoneNumbers != null)
        {
            List<Contact.Number> phoneNumberList = new ArrayList<Contact.Number>(
                    phoneNumbers.getCount());
            while (phoneNumbers.moveToNext())
            {

                String phone = phoneNumbers
                        .getString(phoneNumbers
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                if (phone == null)
                {
                    phone = "0";
                }

                phoneNumberList
                        .add(new Contact.Number(
                                phoneNumbers
                                        .getString(phoneNumbers
                                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                Integer.parseInt(phone)));
            }
            contact.setPhoneNumbers(phoneNumberList);
            phoneNumbers.close();
        }
    }

    private void collectPhotoData(Contact contact, long contactId)
    {
        Uri contactUri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        contact.setPhotoBytes(getPhotoBytes(contactUri));
    }


    private byte[] getPhotoBytes(Uri person)
    {
        byte[] bytes = null;

        InputStream iStream = ContactsContract.Contacts
                .openContactPhotoInputStream(mContentResolver, person, false);
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        if (iStream != null)
        {
            try
            {
                byte[] buffer = new byte[1024];
                int count = 0;

                while ((count = iStream.read(buffer)) > 0)
                {
                    oStream.write(buffer, 0, count);
                }
                bytes = oStream.toByteArray();
            }
            catch (IOException ioException)
            {
                // there is no picture, don't care
            }
            finally
            {
                try
                {
                    oStream.close();
                    iStream.close();
                }
                catch (Exception e)
                {
                }
            }
        }

        return bytes;
    }
}
