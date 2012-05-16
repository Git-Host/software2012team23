package at.tugraz.ist.akm.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import at.tugraz.ist.akm.content.Config.Content;
import at.tugraz.ist.akm.providers.ConfigContentProvider;

public class StandardSettings {
	private static ContentResolver mContentResolver;
	
	public static final Uri URI = Uri.withAppendedPath(Content.CONTENT_URI, ConfigContentProvider.CONFIGURATION_TABLE_NAME);
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String PORT = "port";
	public static final String PROTOCOL = "protocol";
	public static final String KEYSTOREPASSWORD = "keystorepassword";
	
	public static void setStandardSettings(Context context) {
		mContentResolver = context.getContentResolver();
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
		mContentResolver.insert(URI, values);
	}
}
