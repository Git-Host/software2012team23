package at.tugraz.ist.akm.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class Config {
	private ContentResolver mContentResolver;
	
	public Config(Context context) {
		mContentResolver = context.getContentResolver();
	}
	
	
	//TODO: getUserName
	
	public void setUserName(String userName) {
		ContentValues values = new ContentValues();
		values.put(Content.VALUE, userName);
		this.updateSettings(values, StandardSettings.USERNAME);
	}
	
	//TODO: getPassword
	
	public void setPassword(String password) {
		ContentValues values = new ContentValues();
		values.put(Content.VALUE, password);
		this.updateSettings(values, StandardSettings.PASSWORD);
	}
	
	//TODO: getPort
	
	public void setPort(String port) {
		ContentValues values = new ContentValues();
		values.put(Content.VALUE, port);
		this.updateSettings(values, StandardSettings.PORT);
	}
	
	//TODO: getProtocol
	
	public void setProtocol(String protocol) {
		ContentValues values = new ContentValues();
		values.put(Content.VALUE, protocol);
		this.updateSettings(values, StandardSettings.PROTOCOL);
	}
	
	//TODO: getKeyStorePassword
	
	public void setKeyStorePassword(String keyStorePassword) {
		ContentValues values = new ContentValues();
		values.put(Content.VALUE, keyStorePassword);
		this.updateSettings(values, StandardSettings.KEYSTOREPASSWORD);
	}
	
	private void updateSettings(ContentValues values, String where) {
		mContentResolver.update(StandardSettings.URI, values, Content.NAME, new String[] {where});
	}
	
	public static final class Content implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://at.tugraz.ist.akm.providers.ConfigContentProvider");
		
		public static final String CONTENT_TYPE = "at.tugraz.ist.akm.content.Config";
		
		public static final String _ID = "_id";
		
		public static final String NAME = "name";
		
		public static final String VALUE = "value";
	}
}
