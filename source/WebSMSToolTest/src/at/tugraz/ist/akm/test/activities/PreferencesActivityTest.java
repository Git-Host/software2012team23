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

package at.tugraz.ist.akm.test.activities;

import java.io.IOException;
import java.security.cert.X509Certificate;

import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.preferences.PreferencesActivity;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.test.keystore.ApplicationKeyStoreTest;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

import com.robotium.solo.Solo;

public class PreferencesActivityTest extends
        ActivityInstrumentationTestCase2<PreferencesActivity>
{

    private LogClient mLog = new LogClient(
            PreferencesActivityTest.class.getName());


    public PreferencesActivityTest()
    {
        super(PreferencesActivity.class);
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    public void test_startPreferencesActivity()
    {
        PreferencesActivity prefs = getActivity();
        assertTrue(prefs != null);
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mLog.debug(getName() + ".setUp()");

    }


    @Override
    protected void tearDown() throws Exception
    {
        mLog.debug(getName() + ".tearDown()");
        super.tearDown();
    }


    private void setUsernamePasswort(Solo preferencesSolo, String username,
            String password)
    {
        if (false == preferencesSolo.isCheckBoxChecked(0))
        {
            preferencesSolo.clickOnCheckBox(0);
        }
        preferencesSolo.clickOnText(getActivity().getString(
                R.string.preferences_username));
        preferencesSolo.clearEditText(0);
        preferencesSolo.enterText(0, username);
        preferencesSolo.clickOnButton("OK");
        preferencesSolo.clickOnText(getActivity().getString(
                R.string.preferences_password));
        preferencesSolo.clearEditText(0);
        preferencesSolo.enterText(0, password);
        preferencesSolo.clickOnButton("OK");
    }


    public void test_checkboxDisabledIfNoCredentials()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());
        setUsernamePasswort(preferencesSolo, "", "");

        // click elsewhere
        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.clickOnCheckBox(1);

        assertFalse(preferencesSolo.isCheckBoxChecked(0));
    }


    public void test_checkboxDisabledIfPasswordEmpty()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());
        setUsernamePasswort(preferencesSolo, "asdf", "");

        // click elsewhere
        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.clickOnCheckBox(1);

        assertFalse(preferencesSolo.isCheckBoxChecked(0));
        ;
    }


    public void test_checkboxDisabledIfUsernameEmpty()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());
        setUsernamePasswort(preferencesSolo, "", "asdf");

        // click elsewhere
        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.clickOnCheckBox(1);

        assertFalse(preferencesSolo.isCheckBoxChecked(0));
    }


    public void test_checkboxEnabledWithUsernameAndPasswordNotEmpty()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());
        setUsernamePasswort(preferencesSolo, "asdf", "asdfasdf");

        // click elsewhere
        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.clickOnCheckBox(1);

        assertTrue(preferencesSolo.isCheckBoxChecked(0));
    }


    public void test_setPortAboveMaximum()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());

        preferencesSolo
                .clickOnText(resourceString(R.string.preferences_server_port));
        preferencesSolo.clearEditText(0);
        preferencesSolo.enterText(0, "65536");
        preferencesSolo.clickOnButton("OK");
        preferencesSolo.finishOpenedActivities();

        SharedPreferencesProvider prefsProvider = new SharedPreferencesProvider(
                getActivity().getApplicationContext());

        assertEquals(65535, prefsProvider.getPort());
        try
        {
            prefsProvider.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
    }


    public void test_setPortBelowMinimum()
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());

        preferencesSolo
                .clickOnText(resourceString(R.string.preferences_server_port));
        preferencesSolo.clearEditText(0);
        preferencesSolo.enterText(0, "1023");
        preferencesSolo.clickOnButton("OK");
        preferencesSolo.finishOpenedActivities();

        SharedPreferencesProvider prefsProvider = new SharedPreferencesProvider(
                getActivity().getApplicationContext());

        assertEquals(1024, prefsProvider.getPort());
        try
        {
            prefsProvider.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing preferences provider");
        }
    }


    private String resourceString(int id)
    {
        return getActivity().getApplicationContext().getResources()
                .getString(id);
    }


    public void test_protocolEnabled() throws IOException
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());

        if (preferencesSolo.isCheckBoxChecked(1))
        {
            preferencesSolo.clickOnCheckBox(1);
        }

        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.finishOpenedActivities();

        SharedPreferencesProvider provider = new SharedPreferencesProvider(
                getActivity().getApplicationContext());

        assertEquals("https", provider.getProtocol());
        provider.close();
    }


    public void test_protocolDisabled() throws IOException
    {
        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());

        if (!preferencesSolo.isCheckBoxChecked(1))
        {
            preferencesSolo.clickOnCheckBox(1);
        }

        preferencesSolo.clickOnCheckBox(1);
        preferencesSolo.finishOpenedActivities();

        SharedPreferencesProvider provider = new SharedPreferencesProvider(
                getActivity().getApplicationContext());
        assertEquals("http", provider.getProtocol());
        provider.close();
    }


    public void test_renewCertificate()
    {

        Solo preferencesSolo = new Solo(getInstrumentation(), getActivity());
        ApplicationKeyStore keyStore = new ApplicationKeyStore();
        keyStore.loadKeystore(ApplicationKeyStoreTest
                .getDefaultKeystorePassword(), ApplicationKeyStoreTest
                .getKeystoreFilePath(getActivity().getApplicationContext(),
                        mLog));

        X509Certificate certBefore = keyStore.getX509Certficate();

        if (!preferencesSolo.isCheckBoxChecked(1))
        {
            preferencesSolo.clickOnCheckBox(1);
        }

        preferencesSolo
                .clickOnText(resourceString(R.string.preferences_security_renew_certificate_dialog_preference_title));
        preferencesSolo
                .clickOnButton(resourceString(R.string.preferences_security_renew_certificate_dialog_positive_button));
        preferencesSolo.finishOpenedActivities();

        X509Certificate certAfter = keyStore.getX509Certficate();
        try
        {
            keyStore.close();
        }
        catch (Throwable e)
        {
            mLog.error("failed closing application keystore");
        }

        assertNotSame(certBefore, certAfter);
    }
}
