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
