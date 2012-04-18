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

public class AuthContentProvider extends ContentProvider {

	public static final String AUTHORITY = "at.tugraz.ist.akm.providers.AuthContentProvider";
	
	private static UriMatcher mUriMatcher = null;
	private static String DATABASE_NAME = "users";
	private static String NOTES_TABLE_NAME = "usernames";
	private DataBaseHelper mDataBaseHelper = null;
	private static HashMap<String, String> mUsersMap = null;

	public AuthContentProvider() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI("at.tugraz.ist.akm.auth", "users", 1);
		mUsersMap = new HashMap<String, String>();
	}

	private static class DataBaseHelper extends SQLiteOpenHelper {
		DataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
					+ Users.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Users.USERNAME + " VARCHAR(255)" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE_NAME);
			onCreate(db);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case 1:
			count = db.delete(NOTES_TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != 1) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		long rowId = db.insert(NOTES_TABLE_NAME, Users.USERNAME, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Users.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDataBaseHelper = new DataBaseHelper(getContext());
		return mDataBaseHelper != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (mUriMatcher.match(uri)) {
		case 1:
			qb.setTables(NOTES_TABLE_NAME);
			qb.setProjectionMap(mUsersMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case 1:
			count = db.update(NOTES_TABLE_NAME, values, selection, selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
