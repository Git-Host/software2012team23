package at.tugraz.ist.akm.test.content.query;

import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.ContactQueryBuilder;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.test.WebSMSToolTestcase;

public class ContactQueryBuilderTest extends WebSMSToolTestcase {

	public ContactQueryBuilderTest() {
		super(ContactQueryBuilderTest.class.getSimpleName());
	}

	public void testSetIsStarredFilter() {
		ContactFilter filter = new ContactFilter();
		filter.setIsStarred(true);
		ContactQueryBuilder builder = new ContactQueryBuilder(filter);
		ContentProviderQueryParameters queryParams = builder.getQueryArgs();

		assertTrue(queryParams.uri.equals(ContactsContract.Contacts.CONTENT_URI));
		assertTrue(queryParams.where.compareTo(ContactsContract.Contacts.STARRED
				+ " = ? ") == 0);
		String[] queryArgs = queryParams.like;
		assertTrue(queryArgs[0].compareTo("1") == 0);
	}

	public void testSetId() {
		ContactFilter filter = new ContactFilter();
		filter.setId(115);
		ContactQueryBuilder builder = new ContactQueryBuilder(filter);
		ContentProviderQueryParameters queryParams = builder.getQueryArgs();

		assertTrue(queryParams.where.compareTo(ContactsContract.Contacts._ID + " = ? ") == 0);
		String[] queryArgs = queryParams.like;
		assertTrue(queryArgs[0].compareTo("115") == 0);
	}

	public void testSetHasPhoneNumber() {
		ContactFilter filter = new ContactFilter();
		filter.setWithPhone(true);
		ContactQueryBuilder builder = new ContactQueryBuilder(filter);
		ContentProviderQueryParameters queryParams = builder.getQueryArgs();

		assertTrue(queryParams.uri.equals(ContactsContract.Contacts.CONTENT_URI));
		assertTrue(queryParams.where.compareTo(ContactsContract.Contacts.HAS_PHONE_NUMBER
				+ " = ? ") == 0);
		String[] queryArgs = queryParams.like;
		assertTrue(queryArgs[0].compareTo("1") == 0);
	}

	public void testSetMultipleAttributes() {
		ContactFilter filter = new ContactFilter();
		filter.setId(123);
		filter.setIsStarred(true);
		filter.setWithPhone(true);

		ContactQueryBuilder builder = new ContactQueryBuilder(filter);
		ContentProviderQueryParameters queryParams = builder.getQueryArgs();

		assertTrue(queryParams.uri.equals(ContactsContract.Contacts.CONTENT_URI));
		assertTrue(queryParams.where.compareTo(ContactsContract.Contacts._ID
				+ " = ?  AND " + ContactsContract.Contacts.STARRED
				+ " = ?  AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER
				+ " = ? ") == 0);

		String[] queryArgs = queryParams.like;
		assertTrue(queryArgs[0].compareTo("123") == 0);
		assertTrue(queryArgs[1].compareTo("1") == 0);
		assertTrue(queryArgs[2].compareTo("1") == 0);
	}
}
