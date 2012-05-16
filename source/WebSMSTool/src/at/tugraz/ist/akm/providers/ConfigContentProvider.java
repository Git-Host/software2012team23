package at.tugraz.ist.akm.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.content.StandardSettings;
import at.tugraz.ist.akm.trace.Logable;

public class ConfigContentProvider extends ContentProvider {

	public static final String AUTHORITY = "at.tugraz.ist.akm.providers.ConfigContentProvider";
	private static String DATABASE_NAME = "ConfigContent";
	public static final String CONFIGURATION_TABLE_NAME = "config";
	
	private static final UriMatcher uriMatcher;
	private static HashMap<String, String> contentMap;
	private DataBaseHelper dbHelper;
	
	private Logable mLog = new Logable(getClass().getSimpleName());
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case 1:
			try {
			count = db.delete(CONFIGURATION_TABLE_NAME, selection + "=?", selectionArgs);
			db.close();
			}
			catch (Exception e) {
				
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != 1) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = 0;
			
		rowId = db.insert(CONFIGURATION_TABLE_NAME, null, values);
		db.close();
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Config.Content.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case 1:
			try {
				qb.setTables(CONFIGURATION_TABLE_NAME);
				qb.setProjectionMap(contentMap);
			}
			catch (Exception e) {
				
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		
		try {
			c = qb.query(db, projection, selection + "=?", selectionArgs, null,
					null, sortOrder);
			db.close();
	
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		catch (Exception e) {
			mLog.logV("Query" + e.toString());
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case 1:
			try {
			count = db.update(CONFIGURATION_TABLE_NAME, values, selection + "=?", selectionArgs);
			db.close();
			}
			catch (Exception e) {
				
			}
			break;
			

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case 1:
			return Config.Content.CONTENT_TYPE;
		
		default: 
			throw new IllegalArgumentException("unknown URI" + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mLog.logV("onCreate config-content-provider");
		dbHelper = new DataBaseHelper(getContext());
		return !dbHelper.equals(null);
	}
		
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, CONFIGURATION_TABLE_NAME, 1);
		
		contentMap = new HashMap<String, String>();
		contentMap.put(Config.Content._ID, Config.Content._ID);
		contentMap.put(Config.Content.NAME, Config.Content.NAME);
		contentMap.put(Config.Content.VALUE, Config.Content.VALUE);
	}
	
	private static class DataBaseHelper extends SQLiteOpenHelper {
		private Logable mLog = new Logable(getClass().getSimpleName());
		private Context mContext = null;
		DataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
			this.mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mLog.logV("onCreate sqlitedatabase invoked");
			db.execSQL("CREATE TABLE " + CONFIGURATION_TABLE_NAME + " ("
					+ Config.Content._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Config.Content.NAME + " VARCHAR(255),"
					+ Config.Content.VALUE + " VARCHAR(255)" + ");");
			StandardSettings.setStandardSettings(mContext);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + CONFIGURATION_TABLE_NAME);
			onCreate(db);
		}
	}
}
