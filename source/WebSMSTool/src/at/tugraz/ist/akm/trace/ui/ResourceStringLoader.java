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

package at.tugraz.ist.akm.trace.ui;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import at.tugraz.ist.akm.R;

public class ResourceStringLoader implements Closeable
{
    Context mContext = null;


    @SuppressWarnings("unused")
    private ResourceStringLoader()
    {
    }


    public ResourceStringLoader(Context context)
    {
        mContext = context;
    }


    @Override
    public void close() throws IOException
    {
        mContext = null;
    }


    public String getLoginTitle()
    {
        return loadResourceString(R.string.event_list_login_title);
    }


    public String getLoginSuccess()
    {
        return loadResourceString(R.string.event_list_login_success);
    }


    public String getLoginFailed()
    {
        return loadResourceString(R.string.event_list_login_failed);
    }


    public String getSentTitle()
    {
        return loadResourceString(R.string.event_list_smssent_title);
    }


    public String getReceivedTitle()
    {
        return loadResourceString(R.string.event_list_smsreceived_title);
    }


    public String getSettingsChangedTitle()
    {
        return loadResourceString(R.string.event_list_settingts_changed_title);
    }


    public String getSettingsChanged()
    {
        return loadResourceString(R.string.event_list_settingts_changed);
    }


    public String getServiceTitle()
    {
        return loadResourceString(R.string.event_list_service_title);
    }


    public String getServiceStarted()
    {
        return loadResourceString(R.string.event_list_service_started);
    }


    public String getServiceStopped()
    {
        return loadResourceString(R.string.event_list_service_stopped);
    }


    private String loadResourceString(int stringResourceId)
    {
        return mContext.getResources().getString(stringResourceId);
    }
}
