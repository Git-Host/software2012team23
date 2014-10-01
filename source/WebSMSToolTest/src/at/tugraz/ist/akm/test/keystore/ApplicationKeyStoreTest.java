package at.tugraz.ist.akm.test.keystore;

import android.content.Context;
import android.test.AndroidTestCase;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class ApplicationKeyStoreTest extends AndroidTestCase
{

    private LogClient mLog = new LogClient(this);
    
    @Override
    protected void setUp() throws Exception
    {
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    public void test_loadMissingKeystore()
    {
        mLog.debug("remove keystore");        
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath(mContext, mLog));
        
        mLog.debug("load missing keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
    }


    public void test_loadExistingKeystore()
    {
        mLog.debug("remove keystore");
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath(mContext, mLog));
        
        mLog.debug("load missing keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length == 1);
        appKeystore.close();

        mLog.debug("load available keystore");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length == 1);
    }


    public void test_loadKeystore_wrongPassword()
    {
        mLog.debug("remove keystore");
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath(mContext, mLog));
        
        mLog.debug("crate new keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
        
        mLog.debug("load available keystore using wrong password");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getNewRandomKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
    }

    public void test_getCertificateSerial()
    {
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        assertTrue(appKeystore.getX509Certficate() != null);
        appKeystore.close();
    }

    public static String getKeystoreFilePath(Context context, LogClient log)
    {
        
        String filePath = context.getFilesDir().getPath().toString()
                + "/"
                + context.getResources().getString(
                        R.string.preferences_keystore_store_filename);
        log.debug("keystore filepath: " + filePath);
        return filePath;
    }


    public static String getDefaultKeystorePassword()
    {
        return "foobar64";
    }


    private String getNewRandomKeystorePassword()
    {
        return new ApplicationKeyStore().newRandomPassword();
    }
}
