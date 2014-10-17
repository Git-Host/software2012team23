package at.tugraz.ist.akm.preferences;

import java.security.cert.X509Certificate;

import android.content.Context;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;

public class StringFormatter
{

    Context mContext = null;


    @SuppressWarnings("unused")
    private StringFormatter()
    {
    }


    public StringFormatter(Context context)
    {
        mContext = context;
    }
    
    public String certificateDialogSummary()
    {
        String summary = null;
        SharedPreferencesProvider preferencesProvider = new SharedPreferencesProvider(
                mContext);
        ApplicationKeyStore appKeyStore = new ApplicationKeyStore();

        appKeyStore.loadKeystore(preferencesProvider.getKeyStorePassword(),
                preferencesProvider.getKeyStoreFilePath());
        preferencesProvider.close();
        preferencesProvider = null;
        X509Certificate cert = appKeyStore.getX509Certficate();
        appKeyStore.close();

        if (cert != null)
        {
            summary = mContext
                    .getResources()
                    .getString(
                            R.string.OnSharedPreferenceEventListenValidator_serial_number_abbrev)
                    + " "
                    + cert.getSerialNumber().toString()
                    + " "
                    + mContext
                            .getResources()
                            .getString(
                                    R.string.OnSharedPreferenceEventListenValidator_expires)
                    + " " + cert.getNotAfter();
            appKeyStore.close();
        } else
        {
            summary = mContext
                    .getResources()
                    .getString(
                            R.string.preferences_security_renew_certificate_dialog_preference_no_certificate);
        }
        return summary;
    }
    
    public void onClose() {
        mContext = null;
    }
}
