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

import at.tugraz.ist.akm.db.User.Users;
import at.tugraz.ist.akm.trace.Logable;

public class AuthContentProvider extends ContentProvider {

	public static final String AUTHORITY = "at.tugraz.ist.akm.providers.AuthContentProvider";
	private static String DATABASE_NAME = "AuthContent";
	private static String USERS_TABLE_NAME = "users";
	
	private static final UriMatcher uriMatcher;
	private static HashMap<String, String> usersMap;
	private DataBaseHelper dbHelper;
	
	private Logable mLog = new Logable(getClass().getSimpleName());
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case 1:
			try {
			count = db.delete(USERS_TABLE_NAME, selection + "=?", selectionArgs);
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
			return Users.CONTENT_TYPE;
		
		default: 
			throw new IllegalArgumentException("unknown URI" + uri);
		}
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
		long rowId = db.insert(USERS_TABLE_NAME, Users.USERNAME, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Users.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DataBaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case 1:
			try {
				qb.setTables(USERS_TABLE_NAME);
				qb.setProjectionMap(usersMap);
			
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
			
	
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		catch (Exception e) {
			mLog.v("Query" + e.toString());
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
			count = db.update(USERS_TABLE_NAME, values, selection + "=?", selectionArgs);
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
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, USERS_TABLE_NAME, 1);
		
		usersMap = new HashMap<String, String>();
		usersMap.put(Users.USER_ID, Users.USER_ID);
		usersMap.put(Users.USERNAME, Users.USERNAME);
		usersMap.put(Users.PASSWORD, Users.PASSWORD);
	}
	
	private static class DataBaseHelper extends SQLiteOpenHelper {
		DataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + USERS_TABLE_NAME + " ("
					+ Users.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Users.USERNAME + " VARCHAR(255),"
					+ Users.PASSWORD + " VARCHAR(255)" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
			onCreate(db);
		}
	}
}
