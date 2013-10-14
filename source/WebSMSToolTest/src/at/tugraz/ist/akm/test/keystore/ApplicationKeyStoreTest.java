package at.tugraz.ist.akm.test.keystore;

import android.test.AndroidTestCase;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

public class ApplicationKeyStoreTest extends AndroidTestCase
{

    private LogClient mLog = new LogClient(this);
    
    @Override
    protected void setUp() throws Exception
    {
        TraceService.setSink(new ThrowingLogSink());
    }


    public void test_loadMissingKeystore()
    {
        mLog.debug("remove keystore");        
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath());
        
        mLog.debug("load missing keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath()));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
    }


    public void test_loadExistingKeystore()
    {
        mLog.debug("remove keystore");
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath());
        
        mLog.debug("load missing keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath()));
        assertTrue(appKeystore.getKeystoreManagers().length == 1);
        appKeystore.close();

        mLog.debug("load available keystore");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath()));
        assertTrue(appKeystore.getKeystoreManagers().length == 1);
    }


    public void test_loadKeystore_wrongPassword()
    {
        mLog.debug("remove keystore");
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        appKeystore.deleteKeystore(getKeystoreFilePath());
        
        mLog.debug("crate new keystore");
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath()));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
        
        mLog.debug("load available keystore using wrong password");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getNewRandomKeystorePassword(),
                getKeystoreFilePath()));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        appKeystore.close();
    }


    private String getKeystoreFilePath()
    {
        
        String filePath = getContext().getFilesDir().getPath().toString()
                + "/"
                + mContext.getResources().getString(
                        R.string.preferences_keystore_store_filename);
        mLog.debug("keystore filepath: " + filePath);
        return filePath;
    }


    private String getDefaultKeystorePassword()
    {
        return "foobar64";
    }


    private String getNewRandomKeystorePassword()
    {
        return new ApplicationKeyStore().newRandomPassword();
    }
}
