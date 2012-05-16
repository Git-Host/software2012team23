package at.tugraz.ist.akm.content;

import android.net.Uri;
import android.provider.BaseColumns;

public class Config {
	public static final class Content implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://at.tugraz.ist.akm.providers.ConfigContentProvider");
		
		public static final String CONTENT_TYPE = "at.tugraz.ist.akm.content.Config";
		
		public static final String _ID = "_id";
		
		public static final String NAME = "name";
		
		public static final String VALUE = "value";
	}
}
