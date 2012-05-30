package at.tugraz.ist.akm.test.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.content.Config.Content;
import at.tugraz.ist.akm.providers.ConfigContentProvider;
import at.tugraz.ist.akm.test.WebSMSToolTestcase;

public class ConfigContentProviderTest extends WebSMSToolTestcase{

	private Uri uri = Uri.withAppendedPath(Content.CONTENT_URI, ConfigContentProvider.CONFIGURATION_TABLE_NAME);
	public ConfigContentProviderTest() {
		super(ConfigContentProviderTest.class.getSimpleName());
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
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, "bla");
		values.put(Config.Content.VALUE, "bla");
		assertTrue("values not inserted", !(mContentResolver.insert(uri, values) == null));
	}

	public void testInsert2() {
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, "bla");
		values.put(Config.Content.VALUE, "bla");
		assertTrue("values not inserted", !(mContentResolver.insert(uri, values) == null));
	}
	
	public void testDelete() {
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, "bla");
		values.put(Config.Content.VALUE, "bla");
		mContentResolver.insert(uri, values);
		
		String[] names = {"bla"};
		assertTrue("no users deleted", mContentResolver.delete(uri, Config.Content.NAME, names) != 0);
	}
	
	public void testQuery() {
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, "bla");
		values.put(Config.Content.VALUE, "bla");
		mContentResolver.insert(uri, values);
		
		try {
			String[] names = {"bla"};
			Cursor c = mContentResolver.query(uri, null, Config.Content.NAME, names, null);
			assertTrue("no values found", !(c==null));
			c.close();
			Thread.sleep(1000);
		} catch (Exception t) {
			assertTrue(false);
		}
	}

	public void testUpdate() {
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, "bla");
		values.put(Config.Content.VALUE, "bla");
		mContentResolver.insert(uri, values);
		values.clear();
		
		values.put(Config.Content.NAME, "bla2");
		
		String[] names = {"bla"};
		
		assertTrue("no values updated", mContentResolver.update(uri, values, Config.Content.NAME, names) != 0);
	}
	
	public void testGetType() {
		String type = mContentResolver.getType(uri);
		assertTrue("type is not equal to <at.tugraz.ist.akm.content.Config>", type.equals("at.tugraz.ist.akm.content.Config"));
	}
}
