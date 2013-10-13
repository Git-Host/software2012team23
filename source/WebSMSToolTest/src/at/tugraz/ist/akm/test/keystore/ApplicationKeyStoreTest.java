package at.tugraz.ist.akm.test.keystore;

import java.io.File;

import javax.net.ssl.KeyManager;

import android.test.AndroidTestCase;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.TraceService;

public class ApplicationKeyStoreTest extends AndroidTestCase
{

    private String mKeystoreFilePath = null;


    @Override
    protected void setUp() throws Exception
    {
        TraceService.setSink(new ThrowingLogSink());
        mKeystoreFilePath = getKeystoreFileDir();
    }


    public void test_createAndLoadKeystore()
    {
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        String password = appKeystore.newRandomPassword();
        assertTrue(appKeystore.loadKeystore(password, mKeystoreFilePath));
        KeyManager[] ksm = appKeystore.getKeystoreManagers();
        assertTrue(ksm.length > 0);
    }


    public void test_wipeCreateAndLoadKeystore()
    {
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();
        String password = appKeystore.newRandomPassword();
        assertTrue(appKeystore.loadKeystore(password, mKeystoreFilePath));
        assertTrue(appKeystore.loadKeystore(password, mKeystoreFilePath));
        assertTrue(appKeystore.loadKeystore(password, mKeystoreFilePath));
        KeyManager[] ksm = appKeystore.getKeystoreManagers();
        assertTrue(ksm.length > 0);
    }


    public void test_loadMissingKeyStore()
    {
        File file = new File(mKeystoreFilePath);
        if (file.exists())
        {
            file.delete();
        }

        ApplicationKeyStore appKeyStore = new ApplicationKeyStore();
        String password = appKeyStore.newRandomPassword();
        appKeyStore.loadKeystore(password, mKeystoreFilePath);

        assertTrue(file.exists());

    }


    public void test_closeAndCleanupKeystore()
    {
        assertTrue(false);
    }


    private String getKeystoreFileDir()
    {
        return getContext().getFilesDir().getPath().toString() + "/" + mContext.getResources().getString(
                R.string.preferences_keystore_store_filename);
    }
}
