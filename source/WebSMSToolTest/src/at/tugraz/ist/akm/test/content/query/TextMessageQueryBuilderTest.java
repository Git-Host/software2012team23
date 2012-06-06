package at.tugraz.ist.akm.test.content.query;

import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.content.query.TextMessageQueryBuilder;
import at.tugraz.ist.akm.test.WebSMSToolTestcase;

public class TextMessageQueryBuilderTest extends WebSMSToolTestcase {

	public TextMessageQueryBuilderTest() {
		super(TextMessageQueryBuilderTest.class.getSimpleName());
	}

	public void testSetAddress() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.INBOX_URI);
		filter.setAddress("01906666");
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.INBOX_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.ADDRESS + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("01906666") == 0);
	}

	public void testSetId() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.BASE_URI);
		filter.setId(1234);
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.BASE_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.ID + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("1234") == 0);
	}

	public void testSetPerson() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.DRAFT_URI);
		filter.setPerson("foo bar");
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.DRAFT_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.PERSON + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("foo bar") == 0);
	}

	public void testSetRead() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.FAILED_URI);
		filter.setRead(true);
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.FAILED_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.READ + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("1") == 0);
	}

	public void testSetSeen() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.QUEUED_URI);
		filter.setSeen(true);
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.QUEUED_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.SEEN + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("1") == 0);
	}

	public void testSetThreadId() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.UNDELIVERED_URI);
		filter.setThreadId(42L);
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.UNDELIVERED_URI));
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.THREAD_ID + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("42") == 0);
	}
	
	public void testSetMultipleQueries() {
		TextMessageFilter filter = new TextMessageFilter();
		filter.setBox(SmsContent.ContentUri.UNDELIVERED_URI);
		filter.setAddress("01906666");
		filter.setBox(SmsContent.ContentUri.SENT_URI);
		filter.setId(10987L);
		filter.setThreadId(42L);
		filter.setPerson("dr. akula");
		filter.setSeen(false);
		filter.setRead(false);
		TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

		assertTrue(queryParameters.uri.equals(SmsContent.ContentUri.SENT_URI));
		logVerbose(queryParameters.where + "|" + SmsContent.Content.ADDRESS + " = ?  AND " +
				SmsContent.Content.THREAD_ID + " = ?  AND " +
				SmsContent.Content.ID + " = ?  AND " +
				SmsContent.Content.PERSON + " = ?  AND " +
				SmsContent.Content.SEEN + " = ?  AND " +
				SmsContent.Content.READ + " = ? ");
		assertTrue(queryParameters.where.compareTo(SmsContent.Content.ADDRESS + " = ?  AND " +
				SmsContent.Content.THREAD_ID + " = ?  AND " +
				SmsContent.Content.ID + " = ?  AND " +
				SmsContent.Content.PERSON + " = ?  AND " +
				SmsContent.Content.SEEN + " = ?  AND " +
				SmsContent.Content.READ + " = ? ") == 0);
		String[] queryArgs = queryParameters.like;
		assertTrue(queryArgs[0].compareTo("01906666") == 0);
		assertTrue(queryArgs[1].compareTo("42") == 0);
		assertTrue(queryArgs[2].compareTo("10987") == 0);
		assertTrue(queryArgs[3].compareTo("dr. akula") == 0);
		assertTrue(queryArgs[4].compareTo("0") == 0);
		assertTrue(queryArgs[5].compareTo("0") == 0);
		
	}

}
