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

package at.tugraz.ist.akm.test.testdata;

import java.util.Vector;

import at.tugraz.ist.akm.phonebook.contact.Contact;

public class DefaultContacts
{

    private int mEnlargementFactor = 5;
    private int mEnlargementFactorNegativeContacts = 2;


    public String[][] getDefaultRecords()
    {
        String[][] baseRecords = { { "Foo", "Bar", "01906666" },
                { "Fraunz", "Huaba", "43680123456" },
                { "Sepp", "Schnoacha", "0680123457" },
                { "Randy", "Andy", "43680123458" }, { "Pope", "Joke", "6666" },
                { "Franziska van Drüben", "Venus", "43680664658" } };

        return enlarge(baseRecords, mEnlargementFactor);
    }


    public String[][] getNegativeRecords()
    {
        String[][] baseRecords = { { "RandyRandy", "AndyAndy", "43680123458999" } };
        return enlarge(baseRecords, mEnlargementFactorNegativeContacts);
    }


    private String[][] enlarge(String[][] baseRecords, int enlargementFactor)
    {
        int numContacts = baseRecords.length * enlargementFactor;

        String[][] records = new String[numContacts][baseRecords[0].length];

        for (int idx = 0; idx < numContacts; idx++)
        {
            records[idx][0] = baseRecords[idx % baseRecords.length][0];
            records[idx][1] = baseRecords[idx % baseRecords.length][1] + "-"
                    + Integer.toString(idx);
            records[idx][2] = baseRecords[idx % baseRecords.length][2]
                    + Integer.toString(idx);
        }
        return records;
    }


    public Vector<Contact> toContacts(String[][] records)
    {
        Vector<Contact> contacts = new Vector<Contact>();

        for (int idx = 0; idx < records.length; idx++)
        {
            Contact c = new Contact();

            c.setDisplayName(records[idx][0] + " " + records[idx][1]);
            Vector<Contact.Number> numbers = new Vector<Contact.Number>();
            numbers.add(new Contact.Number(records[idx][2], 1));
            c.setPhoneNumbers(numbers);
            c.setStarred(true);
            c.setId(idx);
            contacts.add(c);
        }
        return contacts;
    }
}
