package at.tugraz.ist.akm.content;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import at.tugraz.ist.akm.providers.ConfigContentProvider;

public class StandardSettings {
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
