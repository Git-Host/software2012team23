package at.tugraz.ist.akm.test.texting;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.SmsSentBroadcastReceiver;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;
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
		mIsTestcaseSendLongText = true;
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		TextMessage m = SmsHelper.getDummyMultiTextMessage();
		texting.sendTextMessage(m);

		// try check for callbacks, else wait some time
		int awaitedCallbacks = 3, tries = 30, durationMs = 200;
		while ((tries-- >= 0) && (mCountSent < awaitedCallbacks))
		{
			Thread.sleep(durationMs);
		}

		assertTrue(mCountSent >= awaitedCallbacks);
		texting.stop();
	}

	public void testSendNoException() throws Exception
	{
		mIsTestcaseSendNoFail = true;
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		TextMessage m = new TextMessage();
		m.setAddress("01234");
		m.setBody("foobar foo baz");
		texting.sendTextMessage(m);
		Thread.sleep(1000);
		assertTrue(mCountSent > 0);
		texting.stop();
	}

	public void testFetchContactsNoException()
	{
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		ContactFilter filter = new ContactFilter();
		filter.setId(437);
		List<Contact> contacts = texting.fetchContacts(filter);

		assertTrue(contacts.size() == 1 | contacts.size() == 0);
		// Contact c = contacts.get(0);
		// assertTrue(c.getPhotoBytes() != null);
		// assertTrue(c.getPhotoUri() != null);

		texting.stop();
	}

	public void testFetchMessagesNoException()
	{
		TextingInterface texting = new TextingAdapter(mContext, this, this);
		texting.start();
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.INBOX_URI);
		filter.setAddress("01906666");
		texting.fetchTextMessages(filter);
		texting.stop();
	}

	@Override
	public void contactModifiedCallback()
	{
		logV("contact modified");
	}

	@Override
	public void smsReceivedCallback(Context context, Intent intent)
	{
		logV("sms received (action: " + intent.getAction() + " )");
	}

	@Override
	public void smsSentCallback(Context context, Intent intent)
	{
		++mCountSent;

		Bundle textMessageInfos = intent.getExtras();
		String sentPart = textMessageInfos.getString(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_PART);
		TextMessage m = (TextMessage) textMessageInfos
				.getSerializable(SmsSentBroadcastReceiver.EXTRA_BUNDLE_KEY_TEXTMESSAGE);
		logV("sent part: " + sentPart);

		if (mIsTestcaseSendNoFail)
		{
			assertTrue(0 == sentPart.compareTo("foobar foo baz"));
			assertTrue(0 == m.getAddress().compareTo("01234"));
			assertTrue(0 == m.getBody().compareTo("foobar foo baz"));
		}
		else if (mIsTestcaseSendLongText)
		{
			assertTrue(0 == m.getAddress().compareTo("13570"));
			if (mCountSent == 1)
			{
				assertTrue(sentPart
						.contains(". 123456789012345678901234567890123456789012345678901234567890123456789012345678901234567"));
			}
			else if (mCountSent == 2)
			{
				assertTrue(m
						.getBody()
						.contains(
								"890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
			}
			else if (mCountSent == 3)
			{
				assertTrue(m.getBody().contains("1234567890"));

			}
			else
				assertTrue(false);
		}
		else
		{
			assertTrue(false);
		}
	}

	@Override
	public void smsSentErrorCallback(Context context, Intent intent)
	{
		logV("sms sent erroneous (action: " + intent.getAction() + " )");
	}

	@Override
	public void smsDeliveredCallback(Context context, Intent intent)
	{
		logV("sms delivered (action: " + intent.getAction() + " )");
	}

}
