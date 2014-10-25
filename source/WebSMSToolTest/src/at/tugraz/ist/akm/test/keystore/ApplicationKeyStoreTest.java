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
        super.setUp();
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
        tryCloseAppKeystore(appKeystore);
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
        tryCloseAppKeystore(appKeystore);

        mLog.debug("load available keystore");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length == 1);
        tryCloseAppKeystore(appKeystore);
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
        tryCloseAppKeystore(appKeystore);

        mLog.debug("load available keystore using wrong password");
        appKeystore = new ApplicationKeyStore();
        assertTrue(appKeystore.loadKeystore(getNewRandomKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        tryCloseAppKeystore(appKeystore);
    }


    public void test_getCertificateSerial()
    {
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();

        assertTrue(appKeystore.loadKeystore(getDefaultKeystorePassword(),
                getKeystoreFilePath(mContext, mLog)));
        assertTrue(appKeystore.getKeystoreManagers().length > 0);
        assertTrue(appKeystore.getX509Certficate() != null);
        tryCloseAppKeystore(appKeystore);
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


    private void tryCloseAppKeystore(ApplicationKeyStore keystore)
    {
        try
        {
            keystore.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing application keystore");
        }
    }


    private String getNewRandomKeystorePassword()
    {
        ApplicationKeyStore store = new ApplicationKeyStore();
        String newRandom = store.newRandomPassword();
        tryCloseAppKeystore(store);
        return newRandom;
    }
}
