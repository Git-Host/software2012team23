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

package at.tugraz.ist.akm.content;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import at.tugraz.ist.akm.providers.ConfigContentProvider;

public class Config
{
    public final static Uri CONTENT_URI = Uri
            .parse("content://" + ConfigContentProvider.AUTHORITY);

    private ContentResolver mContentResolver;
    private Uri mUri = Uri.withAppendedPath(Content.CONTENT_URI,  ConfigContentProvider.CONFIGURATION_TABLE_NAME);


    public Config(Context context)
    {
        mContentResolver = context.getContentResolver();
    }


    public String getUserName()
    {
        return this.getSettings(DefaultPreferences.USERNAME);
    }


    public void setUserName(String userName)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, userName);
        this.updateSettings(values, DefaultPreferences.USERNAME);
    }


    public String getPassWord()
    {
        return this.getSettings(DefaultPreferences.PASSWORD);
    }


    public void setPassword(String password)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, password);
        this.updateSettings(values, DefaultPreferences.PASSWORD);
    }


    public String getPort()
    {
        return this.getSettings(DefaultPreferences.PORT);
    }


    public void setPort(String port)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, port);
        this.updateSettings(values, DefaultPreferences.PORT);
    }


    public String getProtocol()
    {
        return this.getSettings(DefaultPreferences.PROTOCOL);
    }


    public void setProtocol(String protocol)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, protocol);
        this.updateSettings(values, DefaultPreferences.PROTOCOL);
    }


    public String getKeyStorePassword()
    {
        return this.getSettings(DefaultPreferences.KEYSTOREPASSWORD);
    }


    public void setKeyStorePassword(String keyStorePassword)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, keyStorePassword);
        this.updateSettings(values, DefaultPreferences.KEYSTOREPASSWORD);
    }


    private String getSettings(String name)
    {
        String[] names = { name };
        String queriedValue = "";
        Cursor cursor = mContentResolver.query(this.mUri,
                new String[] { Content.VALUE }, Content.NAME, names, null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                queriedValue = cursor.getString(0);
            }
            cursor.close();
        }

        return queriedValue;
    }


    private int updateSettings(ContentValues values, String where)
    {
        return mContentResolver.update(this.mUri, values, Content.NAME,
                new String[] { where });
    }

    public static final class Content implements BaseColumns
    {
        public static final Uri CONTENT_URI = Config.CONTENT_URI;
        public static final String CONTENT_TYPE = "at.tugraz.ist.akm.content.Config";
        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }
}
