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

package at.tugraz.ist.akm.test.providers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.content.Config.Content;
import at.tugraz.ist.akm.content.DefaultPreferences;
import at.tugraz.ist.akm.providers.ConfigContentProvider;
import at.tugraz.ist.akm.test.base.WebSMSToolInstrumentationTestcase;

public class ConfigContentProviderTest extends WebSMSToolInstrumentationTestcase{

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
		
		mContentResolver.delete(uri, Config.Content.NAME, new String[] {"bla"});
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
		try {
			String[] names = {DefaultPreferences.PORT};
			Cursor cursor = mContentResolver.query(uri, new String[] {Config.Content.VALUE}, Config.Content.NAME, names, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					logDebug(cursor.getString(0));
				}
			}

			assertTrue("no values found", !(cursor==null));
			cursor.close();
			Thread.sleep(1000);
		} catch (Exception ex) {
			assertTrue(false);
		}
	}

	public void testUpdate() {
		ContentValues values = new ContentValues();
		values.put(Config.Content.VALUE, "admin");
		
		String[] names = {DefaultPreferences.USERNAME};
		
		assertTrue("no values updated at first update", mContentResolver.update(uri, values, Config.Content.NAME, names) != 0);
		values.clear();
		values.put(Config.Content.VALUE, "");
		assertTrue("no values updated at second update", mContentResolver.update(uri, values, Config.Content.NAME, names) != 0);
	}
	
	public void testGetType() {
		String type = mContentResolver.getType(uri);
		assertTrue("type is not equal to <at.tugraz.ist.akm.content.Config>", type.equals("at.tugraz.ist.akm.content.Config"));
	}
}
