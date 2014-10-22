/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PrivateApplicationContentProvider extends SQLiteOpenHelper
{

    private static PrivateApplicationContentProvider mInstance = null;
    private static long mOpenCounter = 0;

    private SQLiteDatabase mReadableDatabaseConnection = null;
    private SQLiteDatabase mWriteablaDatabaseConnection = null;

    private static class Database
    {
        static String NAME = "private-settings.sqlite";

        static class ValuesTable
        {
            static String NAME = "KeyValuePairs";

            static class Domain
            {
                static String KEY = "key";
                static String VALUE = "value";
            }

            static class KeyName
            {
                static String KEYSTORE_PASSWORD = "password";
                static String KEYSTORE_FILEPATH = "filepath";
            }

            static String CREATE = "CREATE TABLE IF NOT EXISTS "
                    + ValuesTable.NAME + " (" + ValuesTable.Domain.KEY
                    + " TEXT, " + ValuesTable.Domain.VALUE + " TEXT)";
            static String DROP = "DROP TABLE " + ValuesTable.NAME;
        }
    }


    private PrivateApplicationContentProvider(Context context)
    {
        super(context, Database.NAME, null, 1);
    }


    public static synchronized void construct(Context context)
    {
        if (mInstance == null)
            mInstance = new PrivateApplicationContentProvider(context);
    }


    public static synchronized PrivateApplicationContentProvider instance()
    {

        if (mInstance == null)
            throw new IllegalStateException(
                    PrivateApplicationContentProvider.class.getSimpleName()
                            + " not initialized, call construct(...) first");
        return mInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        createTables(db);
        insertDefaultPairs(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        dropTables(db);
        onCreate(db);
    }


    public int storeKeystorePassword(String password)
    {
        return storeValue(Database.ValuesTable.KeyName.KEYSTORE_PASSWORD,
                password);
    }


    public String restoreKeystorePassword()
    {
        return restoreValue(Database.ValuesTable.KeyName.KEYSTORE_PASSWORD);
    }


    private String restoreValue(String keyName)
    {
        String[] columns = { Database.ValuesTable.Domain.VALUE };
        String where = Database.ValuesTable.Domain.KEY + " = ?";
        String[] whereArgs = { keyName };

        Cursor rows = getReadableDatabaseConnection().query(
                Database.ValuesTable.NAME, columns, where, whereArgs, null,
                null, null);

        if (rows != null)
        {
            if (rows.moveToNext())
            {
                return rows.getString(rows
                        .getColumnIndex(Database.ValuesTable.Domain.VALUE));
            }

            rows.close();
        }
        return "";
    }


    private int storeValue(String keyName, String value)
    {
        ContentValues values = new ContentValues();
        values.put(Database.ValuesTable.Domain.VALUE, value);
        String where = Database.ValuesTable.Domain.KEY + " = ? AND "
                + Database.ValuesTable.Domain.VALUE + " != ?";
        String[] whereArgs = { keyName, value };

        return getWriteableDatabaseConnection().update(
                Database.ValuesTable.NAME, values, where, whereArgs);
    }


    private long insertDefaultPairs(SQLiteDatabase db)
    {
        long affectedRows = 0;
        ContentValues values = new ContentValues();
        values.put(Database.ValuesTable.Domain.KEY,
                Database.ValuesTable.KeyName.KEYSTORE_PASSWORD);
        values.put(Database.ValuesTable.Domain.VALUE, "");
        affectedRows += db.insert(Database.ValuesTable.NAME, null, values);

        values.clear();
        values.put(Database.ValuesTable.Domain.KEY,
                Database.ValuesTable.KeyName.KEYSTORE_FILEPATH);
        values.put(Database.ValuesTable.Domain.VALUE, "");
        affectedRows += db.insert(Database.ValuesTable.NAME, null, values);

        return affectedRows;
    }


    private void dropTables(SQLiteDatabase db)
    {
        db.execSQL(Database.ValuesTable.DROP);
    }


    private void createTables(SQLiteDatabase db)
    {
        db.execSQL(Database.ValuesTable.CREATE);
    }


    @Override
    public void close()
    {
        if (mWriteablaDatabaseConnection != null)
        {
            mWriteablaDatabaseConnection.close();
            mWriteablaDatabaseConnection = null;
        }
        if (mReadableDatabaseConnection != null)
        {
            mReadableDatabaseConnection.close();
            mReadableDatabaseConnection = null;
        }
        super.close();
    }


    private SQLiteDatabase getReadableDatabaseConnection()
    {
        if (mReadableDatabaseConnection == null)
            mReadableDatabaseConnection = getReadableDatabase();
        return mReadableDatabaseConnection;
    }


    private SQLiteDatabase getWriteableDatabaseConnection()
    {
        if (mWriteablaDatabaseConnection == null)
            mWriteablaDatabaseConnection = getWritableDatabase();
        return mWriteablaDatabaseConnection;
    }


    public synchronized void openDatabase()
    {
        mOpenCounter++;
    }


    public synchronized void closeDatabase()
    {
        if (mOpenCounter > 0)
            mOpenCounter--;

        if (mOpenCounter <= 0)
            close();
    }
}
