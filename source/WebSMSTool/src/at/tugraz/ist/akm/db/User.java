package at.tugraz.ist.akm.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class User {
	public static final class Users implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://at.tugraz.ist.akm.providers.AuthContentProvider");
		
		public static final String CONTENT_TYPE = "at.tugraz.ist.akm.db.User";
		
		public static final String USER_ID = "_id";
		
		public static final String USERNAME = "name";
		
		public static final String PASSWORD = "password";
	}
}
