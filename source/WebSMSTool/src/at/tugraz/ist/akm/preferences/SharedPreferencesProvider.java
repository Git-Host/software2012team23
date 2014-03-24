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
import android.graphics.AvoidXfermode.Mode;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.content.DefaultPreferencesInserter;
import at.tugraz.ist.akm.providers.ApplicationContentProvider;

public class SharedPreferencesProvider
{
    private ContentResolver mContentResolver;
    private SharedPreferences mSharedPreferences = null;
    private Context mApplicationContext = null;
    private final static String KEYSTORE_FILE_NAME = "websms-keystore.bks";
    private final static String HTTPS_PROTOCOL_NAME = "https";
    private final static String HTTP_PROTOCOL_NAME = "http";


    public SharedPreferencesProvider(Context context)
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
        if (mSharedPreferences.getBoolean(
                resourceIdString(R.string.preferences_protocol_checkbox_key),
                true))
        {
            return HTTPS_PROTOCOL_NAME;
        }
        return HTTP_PROTOCOL_NAME;
    }


    public void setProtocol(String protocol)
    {
        Boolean isHttps = true;
        if (protocol.equals(HTTP_PROTOCOL_NAME))
        {
            isHttps = false;
        }
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(
                resourceIdString(R.string.preferences_protocol_checkbox_key),
                isHttps);
        editor.apply();
    }


    public String getKeyStorePassword()
    {
        return getSettingFromApplicationContentProvider(DefaultPreferencesInserter.KEYSTOREPASSWORD);
    }


    public void setKeyStorePassword(String keyStorePassword)
    {
        storeSettingToApplicationContentProvider(
                DefaultPreferencesInserter.KEYSTOREPASSWORD, keyStorePassword);
    }


    public String getKeyStoreFilePath()
    {
        return mApplicationContext.getFilesDir().getPath().toString() + "/"
                + KEYSTORE_FILE_NAME;
    }


    private void storeSettingToApplicationContentProvider(String name, String value)
    {
        ContentValues values = new ContentValues();
        values.put(Content.VALUE, value);
        updateSettings(values, name);
    }


    private String getSettingFromApplicationContentProvider(String name)
    {
        String[] names = { name };
        String queriedValue = "";
        Cursor cursor = mContentResolver.query(
                ApplicationContentProvider.PREFERENCES_URI,
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
        return mContentResolver.update(
                ApplicationContentProvider.PREFERENCES_URI, values,
                Content.NAME, new String[] { where });
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
