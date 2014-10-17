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

package at.tugraz.ist.akm.texting;

import java.io.Closeable;
import java.util.List;

import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.reports.VolatileIncomingReport;
import at.tugraz.ist.akm.texting.reports.VolatileOutgoingReport;
import at.tugraz.ist.akm.texting.reports.VolatilePhonebookReport;

public interface TextingInterface extends Closeable
{

    public void start();


    public void stop();


    public int sendTextMessage(TextMessage message);


    public List<TextMessage> fetchTextMessages(TextMessageFilter filter);


    public int updateTextMessage(TextMessage message);


    public List<Contact> fetchContacts(ContactFilter filter);


    public List<Integer> fetchThreadIds(final String address);


    public VolatileIncomingReport getIncomingReport();


    public VolatileOutgoingReport getOutgoingReport();


    public VolatilePhonebookReport getPhonebookReport();

}
