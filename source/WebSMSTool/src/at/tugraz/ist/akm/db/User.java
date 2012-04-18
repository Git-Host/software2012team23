package at.tugraz.ist.akm.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class User {
	public User() {
		
	}
	
	public static final class Users implements BaseColumns {
		private Users() {
			
		}
		
		public static final Uri CONTENT_URI = Uri.parse("content://websmstool/settings/users");
		
		public static final String CONTENT_TYPE = "at.tugraz.ist.akm.db.User";
		
		public static final String USER_ID = "_id";
		
		public static final String USERNAME = "name";
	}
}
