package at.tugraz.ist.akm.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
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
        mStrings = new StringFormatter(mContext);
    }


    @Override
    protected void onAttachedToActivity()
    {
        super.onAttachedToActivity();
        setSummary(mStrings.certificateDialogSummary());
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
        close();
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


    private void close()
    {
        mContext = null;
        if (mStrings != null)
        {
            try
            {
                mStrings.close();
                mStrings = null;
            }
            catch (Throwable e)
            {
                mLog.error("failed closing string formatter");
            }
        }
        mLog = null;
    }

}
