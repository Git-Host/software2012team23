package at.tugraz.ist.akm.texting;

import java.util.List;

import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.reports.VolatileIncomingReport;
import at.tugraz.ist.akm.texting.reports.VolatileOutgoingReport;
import at.tugraz.ist.akm.texting.reports.VolatilePhonebookReport;

public interface TextingInterface {

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