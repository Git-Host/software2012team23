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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.trace.LogClient;

public class RenewCertificateDialogPreference extends DialogPreference
{

    LogClient mLog = new LogClient(
            RenewCertificateDialogPreference.class.getCanonicalName());

    Context mContext = null;
    StringFormatter mStrings = null;


    public RenewCertificateDialogPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        tryLoadFormatter();
        mLog.debug("dialog costructed");
    }


    @Override
    protected View onCreateDialogView()
    {
        mLog.debug("on create dialog view");
        tryLoadFormatter();
        return super.onCreateDialogView();
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        mLog.debug("on restore instance state");
        tryLoadFormatter();
        super.onRestoreInstanceState(state);
    }


    @Override
    public void onActivityDestroy()
    {
        tryCloseFormatter();
        super.onActivityDestroy();
    }


    @Override
    public void onDismiss(DialogInterface dialog)
    {
        tryCloseFormatter();
        super.onDismiss(dialog);
    }


    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        super.onClick(dialog, which);

        if (which == DialogInterface.BUTTON_POSITIVE)
        {
            renewCertificate();
        } else if (which == DialogInterface.BUTTON_NEGATIVE)
        {
        }
        setSummary(mStrings.certificateDialogSummary());
    }


    private void renewCertificate()
    {
        SharedPreferencesProvider preferencesProvider = new SharedPreferencesProvider(
                getContext());
        ApplicationKeyStore appKeyStore = new ApplicationKeyStore();
        appKeyStore.deleteKeystore(preferencesProvider.getKeyStoreFilePath());
        String newPassword = appKeyStore.newRandomPassword();
        appKeyStore.loadKeystore(newPassword,
                preferencesProvider.getKeyStoreFilePath());
        try
        {
            appKeyStore.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing application keystore");
        }
        preferencesProvider.setKeyStorePassword(newPassword);
        try
        {
            preferencesProvider.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
    }


    private void tryLoadFormatter()
    {
        if (mContext != null && mStrings == null)
        {
            mStrings = new StringFormatter(mContext);
        }
    }


    private void tryCloseFormatter()
    {
        if (mStrings != null)
        {
            try
            {
                mStrings.close();
                mStrings = null;
            }
            catch (Throwable e)
            {
                if (mLog != null)
                {
                    mLog.error("failed closing string formatter");
                }
            }
        }
    }
}
