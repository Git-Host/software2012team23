package at.tugraz.ist.akm.test.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.db.User.Users;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class AuthContentProviderTest extends WebSMSToolTestInstrumentation {

	public AuthContentProviderTest() {
		super(AuthContentProviderTest.class.getSimpleName());
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInsert() {
		Uri uri = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider/users");

		ContentValues values = new ContentValues();
		values.put(Users.USERNAME, "bla");
		values.put(Users.PASSWORD, "bla");
		assertTrue("values not inserted", !(mContentResolver.insert(uri, values) == null));
	}

	public void testInsert2() {
		Uri uri = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider/users");

		ContentValues values = new ContentValues();
		values.put(Users.USERNAME, "bla");
		values.put(Users.PASSWORD, "bla");
		assertTrue("values not inserted", !(mContentResolver.insert(uri, values) == null));
	}
	
	public void testDelete() {
		Uri uri = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider/users");
		String[] names = {"bla"};
		
		assertTrue("no users deleted", mContentResolver.delete(uri, Users.USERNAME, names) != 0);
	}
	
	public void testQuery() {
		try {
			Uri uri = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider/users");
			
			String[] names = {"bla"};
			Cursor c = mContentResolver.query(uri, null, Users.USERNAME, names, null);
			assertTrue("no values found", !(c==null));
			c.close();
			Thread.sleep(1000);
		} catch (Exception t) {
			assertTrue(false);
		}
	}

	public void testUpdate() {
		Uri uri = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider/users");
		
		ContentValues values = new ContentValues();
		values.put(Users.USERNAME, "bla2");
		
		String[] names = {"bla"};
		
		assertTrue("no values updated", mContentResolver.update(uri, values, Users.USERNAME, names) != 0);
	}
}
