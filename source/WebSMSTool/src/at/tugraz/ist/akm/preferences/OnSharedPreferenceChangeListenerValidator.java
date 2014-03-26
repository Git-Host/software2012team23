package at.tugraz.ist.akm.preferences;

import java.security.cert.X509Certificate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.trace.LogClient;

public class OnSharedPreferenceChangeListenerValidator implements
        OnSharedPreferenceChangeListener
{
    public static final String CERTIFICATE_RENEWED = "at.tugraz.ist.akm.sms.CERTIFICATE_RENEWED";

    private int mMinPortNumber = 1024;
    private int mMaxPortNumber = 65535;
    private LogClient mLog = new LogClient(this);
    private Context mContext = null;


    public OnSharedPreferenceChangeListenerValidator(Context context)
    {
        mContext = context;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        mLog.debug("shared preferences key changed: [" + key + "]");
        if (key.equals(resourceString(R.string.preferences_server_port_key)))
        {
            int changedPortNumber = 0;
            try
            {
                changedPortNumber = Integer.parseInt(sharedPreferences
                        .getString(key, "8888"));
            }
            catch (NumberFormatException e)
            {
                changedPortNumber = 8888;
                mLog.warning("ignoring invalid user input");
            }

            changedPortNumber = trimPortNumber(changedPortNumber);
            Editor ed = sharedPreferences.edit();
            ed.putString(key, Integer.toString(changedPortNumber));
            ed.apply();
        } else if (key
                .equals(resourceString(R.string.preferences_protocol_renew_certificate_checkbox_key)))
        {
            if (sharedPreferences.getBoolean(key, false) == true)
            {
                Editor ed = sharedPreferences.edit();
                renewCertificate();
                ed.putBoolean(key, false);
                ed.apply();
            }
        }
        else
        {
            mLog.warning("missed preference on state changed event");
        }
    }


    private void renewCertificate()
    {
        SharedPreferencesProvider preferencesProvider = new SharedPreferencesProvider(
                mContext);
        ApplicationKeyStore appKeyStore = new ApplicationKeyStore();
        appKeyStore.deleteKeystore(preferencesProvider.getKeyStoreFilePath());
        String newPassword = appKeyStore.newRandomPassword();
        appKeyStore.loadKeystore(newPassword,
                preferencesProvider.getKeyStoreFilePath());
        X509Certificate newCertificate = appKeyStore.getX509Certficate();
        appKeyStore.close();
        sendKeystoreRenewedNotificationIntent(newCertificate);
        preferencesProvider.setKeyStorePassword(newPassword);
        preferencesProvider.close();
    }


    private void sendKeystoreRenewedNotificationIntent(X509Certificate cert)
    {
        String summary = "error";

        if (cert != null)
        {
            summary = "SN " + cert.getSerialNumber().toString() + " Expires "
                    + cert.getNotAfter();
        }

        Intent i = new Intent(CERTIFICATE_RENEWED);
        i.putExtra(CERTIFICATE_RENEWED, summary);
        i.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        mContext.sendBroadcast(i);
    }


    private int trimPortNumber(int uncheckedNumber)
    {
        int checkedPortNumber = uncheckedNumber;
        if (uncheckedNumber < mMinPortNumber)
        {
            checkedPortNumber = mMinPortNumber;
        } else if (uncheckedNumber > mMaxPortNumber)
        {
            checkedPortNumber = mMaxPortNumber;
        }

        mLog.debug("trimmed from [" + uncheckedNumber + "] to ["
                + checkedPortNumber + "]");
        return checkedPortNumber;
    }


    private String resourceString(int resourceStringId)
    {
        return mContext.getResources().getString(resourceStringId);
    }
    
}
