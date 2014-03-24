package at.tugraz.ist.akm.phonebook;

import java.security.MessageDigest;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import at.tugraz.ist.akm.content.query.ContactFilter;

public class PhonebookCache extends SQLiteOpenHelper implements
        DatabaseErrorHandler
{

    private static class Database
    {
        static String NAME = "contact-cache.sqlite";

        public static class CacheTable
        {
            static String NAME = "PhonebookCache";

            public static class Domain
            {
                static String HASH = "md5";
                static String NAME = "Name";
                static String LAST_NAME = "LastName";
                static String DISPLAY_NAME = "DisplayName";
                static String ID = "ID";
                static String PHONENUMBERS = "PhoneNumbers";
                static String IMAGE = "Image";
            }

            // id, hash, display_name, name, last_name, phone_numbers, image
            static String CREATE = "CREATE TABLE IF NOT EXISTS " + NAME + " ("
                    + Domain.ID + " TEXT, " + Domain.HASH + " TEXT, "
                    + Domain.DISPLAY_NAME + " TEXT, " + Domain.NAME + " TEXT, "
                    + Domain.LAST_NAME + " TEXT, " + Domain.PHONENUMBERS
                    + " TEXT, " + Domain.IMAGE + " BLOB);";
        }

        public static class InfoTable
        {
            static String NAME = "CacheInfos";

            public static class Domain
            {
                static String KEY = "Key";
                static String VALUE = "Value";
            }

            // key, value
            static String CREATE = "CREATE TABLE IF NOT EXISTS " + NAME + " ("
                    + Domain.KEY + " TEXT, " + Domain.VALUE + " TEXT);";
        }
    }


    public PhonebookCache(Context context)
    {
        super(context, Database.NAME, null, 1);
    }


    public long cache(Contact contact)
    {

        return updateOrInsertContact(contact);
    }


    public long numEntries()
    {
        long rowsAffected = 0;
        Cursor rows = getReadableDatabase().rawQuery(
                "select " + Database.CacheTable.Domain.ID + " from "
                        + Database.CacheTable.NAME + " where 1=1 ", null);

        if (rows != null)
        {
            while (rows.moveToNext())
            {
                rowsAffected++;
            }
        }
        return rowsAffected;
    }


    public void clear()
    {
        dropTables();
        onCreate(getReadableDatabase());
    }


    private long updateOrInsertContact(Contact contact)
    {
        long affectedSets = 0;

        affectedSets = updateContactToDatabase(contact);
        if (affectedSets <= 0)
        {
            affectedSets = insertContactToDatabase(contact);
        }
        return affectedSets;
    }


    private int updateContactToDatabase(Contact contact)
    {
        ContentValues values = contactContentValues(contact);
        String where = Database.CacheTable.Domain.ID + " = ?";
        String whereArgs[] = { "" + contact.getId() };
        return getWritableDatabase().update(Database.CacheTable.NAME, values,
                where, whereArgs);

    }


    private long insertContactToDatabase(Contact contact)
    {
        ContentValues values = contactContentValues(contact);
        return getWritableDatabase().insert(Database.CacheTable.NAME, null,
                values);
    }


    private ContentValues contactContentValues(Contact contact)
    {
        String hash = md5sum(stringifyContact(contact));
        ContentValues values = new ContentValues();
        values.put(Database.CacheTable.Domain.ID, contact.getId());
        values.put(Database.CacheTable.Domain.HASH, hash);
        values.put(Database.CacheTable.Domain.DISPLAY_NAME,
                contact.getDisplayName());
        values.put(Database.CacheTable.Domain.NAME, contact.getName());
        values.put(Database.CacheTable.Domain.LAST_NAME,
                contact.getFamilyName());
        values.put(Database.CacheTable.Domain.PHONENUMBERS, contact
                .getPhoneNumbers().toString());
        values.put(Database.CacheTable.Domain.IMAGE, contact.getPhotoBytes());
        return values;
    }


    private String stringifyContact(Contact contact)
    {
        StringBuffer stringified = new StringBuffer();
        stringified.append(contact.getDisplayName()).append(contact.getName())
                .append(contact.getFamilyName())
                .append(contact.getPhoneNumbers().toString())
                .append(contact.getPhotoBytes());
        return stringified.toString();
    }


    private String md5sum(String message)
    {
        StringBuffer digest = new StringBuffer();
        try
        {
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(bytesOfMessage);
            for (byte b : messageDigest)
            {
                digest.append(Integer.toHexString(b));
            }
        }
        catch (Exception e)
        {
            // don't care
        }
        return digest.toString();
    }


    public void onClose()
    {
        getWritableDatabase().close();
    }


    @Override
    public void onCorruption(SQLiteDatabase db)
    {
        dropTables();
        onCreate(db);
    }


    private void dropTables()
    {
        getWritableDatabase().execSQL(
                "DROP TABLE IF EXISTS " + Database.CacheTable.NAME);
        getWritableDatabase().execSQL(
                "DROP TABLE IF EXISTS " + Database.CacheTable.NAME);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(Database.CacheTable.CREATE);
        db.execSQL(Database.InfoTable.CREATE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        dropTables();
        db.execSQL(Database.CacheTable.CREATE);
        db.execSQL(Database.InfoTable.CREATE);
    }


    public List<Contact> getCached(ContactFilter filter)
    {
        String columns[] = { Database.CacheTable.Domain.NAME,
                Database.CacheTable.Domain.LAST_NAME,
                Database.CacheTable.Domain.DISPLAY_NAME,
                Database.CacheTable.Domain.ID,
                Database.CacheTable.Domain.PHONENUMBERS,
                Database.CacheTable.Domain.IMAGE };
        String where = "1=1";
        Cursor rows = getReadableDatabase().query(Database.CacheTable.NAME,
                columns, where, null, null, null, null);

        List<Contact> contacts = new Vector<Contact>();
        if (rows != null)
        {
            while (rows.moveToNext())
            {
                contacts.add(newContactFromCursor(rows));
            }
        }
        return contacts;
    }


    private Contact newContactFromCursor(Cursor cursor)
    {
        Contact c = new Contact();
        c.setDisplayName(cursor.getString(cursor
                .getColumnIndex((Database.CacheTable.Domain.DISPLAY_NAME))));
        c.setName(cursor.getString(cursor
                .getColumnIndex((Database.CacheTable.Domain.NAME))));
        c.setFamilyName(cursor.getString(cursor
                .getColumnIndex((Database.CacheTable.Domain.LAST_NAME))));
        c.setId(cursor.getLong(cursor
                .getColumnIndex((Database.CacheTable.Domain.ID))));
        return c;
    }
}
