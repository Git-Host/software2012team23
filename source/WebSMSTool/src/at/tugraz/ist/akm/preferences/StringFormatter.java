package at.tugraz.ist.akm.preferences;

import java.io.Closeable;
import java.io.IOException;
import java.security.cert.X509Certificate;

import android.content.Context;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.trace.LogClient;

public class StringFormatter implements Closeable
{

    private Context mContext = null;
    private LogClient mLog = new LogClient(
            StringFormatter.class.getCanonicalName());


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
        try
        {
            preferencesProvider.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
        preferencesProvider = null;
        X509Certificate cert = appKeyStore.getX509Certficate();
        try
        {
            appKeyStore.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing application keystore");
        }

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
            try
            {
                appKeyStore.close();
            }
            catch (Throwable e)
            {
                mLog.error("failed closing application keystore");
            }
        } else
        {
            summary = mContext
                    .getResources()
                    .getString(
                            R.string.preferences_security_renew_certificate_dialog_preference_no_certificate);
        }
        return summary;
    }


    @Override
    public void close() throws IOException
    {
        mContext = null;
        mLog = null;
    }
}
