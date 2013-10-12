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

package at.tugraz.ist.akm.preferences;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.content.DefaultPreferences;
import at.tugraz.ist.akm.providers.ApplicationContentProvider;

public class PreferencesProvider
{
    private ContentResolver mContentResolver;
    private SharedPreferences mSharedPreferences = null;
    private Context mApplicationContext = null;


    public PreferencesProvider(Context context)
    {
        mApplicationContext = context;
        mContentResolver = context.getContentResolver();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }


    public String getUserName()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_username_key), "");
    }


    public void setUserName(String userName)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(resourceIdString(R.string.preferences_username_key),
                userName);
        editor.apply();
    }


    private String resourceIdString(int resourceId)
    {
        return mApplicationContext.getString(resourceId);
    }


    public String getPassWord()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_password_key), "");
    }


    public void setPassword(String password)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(resourceIdString(R.string.preferences_password_key),
                password);
        editor.apply();
    }


    public String getPort()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_server_port_key), "-1");
    }


    public void setPort(String port)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(
                resourceIdString(R.string.preferences_server_port_key), port);
        editor.apply();
    }


    public String getProtocol()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_server_protocol_key), "-1");
    }


    public void setProtocol(String protocol)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(
                resourceIdString(R.string.preferences_server_protocol_key),
                protocol);
        editor.apply();
    }


    public String getKeyStorePassword()
    {
        return this
                .getSettingFromContentProvider(DefaultPreferences.KEYSTOREPASSWORD);
    }


    public void setKeyStorePassword(String keyStorePassword)
    {
        putSettingToContentProvider(DefaultPreferences.KEYSTOREPASSWORD,
                keyStorePassword);
    }


    private void putSettingToContentProvider(String name, String value)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, value);
        this.updateSettings(values, name);
    }


    private String getSettingFromContentProvider(String name)
    {
        String[] names = { name };
        String queriedValue = "";
        Cursor cursor = mContentResolver.query(ApplicationContentProvider.PREFERENCES_URI,
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
        return mContentResolver.update(ApplicationContentProvider.PREFERENCES_URI, values, Content.NAME,
                new String[] { where });
    }

    public static final class Content implements BaseColumns
    {
        public static final Uri CONTENT_URI = ApplicationContentProvider.CONTENT_URI;
        public static final String CONTENT_TYPE = "at.tugraz.ist.akm.content.Config";
        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }
}
