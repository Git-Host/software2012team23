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

package at.tugraz.ist.akm.test.texting;

import java.util.List;

import android.content.Context;
import at.tugraz.ist.akm.content.SmsContentConstants;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;
import at.tugraz.ist.akm.test.sms.SmsHelper;
import at.tugraz.ist.akm.texting.TextingAdapter;
import at.tugraz.ist.akm.texting.TextingInterface;

public class TextingAdapterTest extends WebSMSToolActivityTestcase implements SmsIOCallback, ContactModifiedCallback
{

	private int mCountSent = 0;
	private boolean mIsTestcaseSendNoFail = false;
	private boolean mIsTestcaseSendLongText = false;

	public TextingAdapterTest()
	{
		super(TextingAdapterTest.class.getSimpleName());
	}

	public void testSendLongText() throws Exception
	{
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		int awaitedCallbacks = 3, tries = 30, durationMs = 200;
		android.telephony.ServiceState voiceService = new android.telephony.ServiceState();
		
		if ( android.telephony.ServiceState.STATE_IN_SERVICE == voiceService.getState() ) {
		
			mIsTestcaseSendLongText = true;
			texting.start();
			TextMessage message = SmsHelper.getDummyMultiTextMessage();
			texting.sendTextMessage(message);
	
			// try check for callbacks, else wait some time
			while ((tries-- >= 0) && (mCountSent < awaitedCallbacks))
			{
				Thread.sleep(durationMs);
			}
			assertTrue(mCountSent >= awaitedCallbacks);
				texting.stop();
		}
	}

	public void testSendNoException() throws Exception
	{
		android.telephony.ServiceState voiceService = new android.telephony.ServiceState();
		
		if ( android.telephony.ServiceState.STATE_IN_SERVICE == voiceService.getState() ) {
			mIsTestcaseSendNoFail = true;
			TextingInterface texting = new TextingAdapter(mContext, this, this);
			texting.start();
			TextMessage message = new TextMessage();
			message.setAddress("01234");
			message.setBody("foobar foo baz");
			texting.sendTextMessage(message);
			Thread.sleep(1000);
			assertTrue(mCountSent > 0);
			texting.stop();
		}
	}

	public void testFetchContactsNoException()
	{
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		ContactFilter filter = new ContactFilter();
		filter.setId(437);
		List<Contact> contacts = texting.fetchContacts(filter);

		assertTrue(contacts.size() == 1 | contacts.size() == 0);

		texting.stop();
	}

	public void testFetchMessagesNoException()
	{
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContentConstants.Uri.INBOX_URI);
		filter.setAddress("01906666");
		texting.fetchTextMessages(filter);
		texting.stop();
	}

	@Override
	public void contactModifiedCallback()
	{
		logVerbose("contact modified");
	}

	@Override
	public void smsReceivedCallback(Context context, List<TextMessage> messages)
	{
		logVerbose("sms received (messages size: " + messages.size() + " )");
	}

	@Override
	public void smsSentCallback(Context context, List<TextMessage> messages)
	{
		++mCountSent;

		for(TextMessage message : messages){
			if (mIsTestcaseSendNoFail)
			{
				assertTrue(0 == message.getAddress().compareTo("01234"));
				assertTrue(0 == message.getBody().compareTo("foobar foo baz"));
			}
			else if (mIsTestcaseSendLongText)
			{
				assertTrue(0 == message.getAddress().compareTo("13570"));
				if (mCountSent == 1)
				{
					assertTrue(message
							.getBody()
							.contains(". 123456789012345678901234567890123456789012345678901234567890123456789012345678901234567"));
				}
				else if (mCountSent == 2)
				{
					assertTrue(message
							.getBody()
							.contains(
									"890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
				}
				else if (mCountSent == 3)
				{
					assertTrue(message.getBody().contains("1234567890"));
	
				}
				else
					assertTrue(false);
			}
			else
			{
				assertTrue(false);
			}
		}
	}

	@Override
	public void smsSentErrorCallback(Context context, List<TextMessage> messages)
	{
		logError("sms sent erroneous (messages size: " + messages.size() + " )");
	}

	@Override
	public void smsDeliveredCallback(Context context, List<TextMessage> messages)
	{
		logVerbose("sms delivered (messages size: " + messages.size() + " )");
	}

}
