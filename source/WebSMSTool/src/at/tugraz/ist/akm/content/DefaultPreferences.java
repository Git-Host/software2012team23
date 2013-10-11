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

package at.tugraz.ist.akm.content;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import at.tugraz.ist.akm.providers.ConfigContentProvider;

public class DefaultPreferences {
	private static SQLiteDatabase mDb;
	
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String PORT = "port";
	public static final String PROTOCOL = "protocol";
	public static final String KEYSTOREPASSWORD = "keystorepassword";
	
	public static void setStandardSettings(SQLiteDatabase db) {
		mDb = db;
		insertKeyValuePair(USERNAME, "");
		insertKeyValuePair(PASSWORD, "");
		insertKeyValuePair(PORT, "8887");
		insertKeyValuePair(PROTOCOL, "http");
		insertKeyValuePair(KEYSTOREPASSWORD, "foobar64");
	}
	
	private static void insertKeyValuePair(String name, String value) {
		ContentValues values = new ContentValues();
		values.put(Config.Content.NAME, name);
		values.put(Config.Content.VALUE, value);
		mDb.insert(ConfigContentProvider.CONFIGURATION_TABLE_NAME, null, values);
	}
}
