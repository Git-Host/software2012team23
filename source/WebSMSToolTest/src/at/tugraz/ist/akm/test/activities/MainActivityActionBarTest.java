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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.activities.PreferencesActivity;
import at.tugraz.ist.akm.activities.SettingsActivity;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityActionBarTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{

    private LogClient mLog = null;


    public MainActivityActionBarTest()
    {
        super("at.tugraz.ist.akm", MainActivity.class);
        TraceService.setSink(new ThrowingLogSink());
        mLog = new LogClient(MainActivityActionBarTest.class.getName());
    }


    public void testClickAndClosePreferencesActivty()
    {
        clickAndCloseActivty(PreferencesActivity.class);
    }


    public void testClickAndCloseAboutActivty()
    {
        clickAndCloseActivty(PreferencesActivity.class);
    }


    private void clickAndCloseActivty(Class<?> activityClass)
    {
        try
        {
            Instrumentation instrumentation = getInstrumentation();

            Solo mainSolo = new Solo(instrumentation, getActivity());
            mainSolo.assertCurrentActivity(
                    "current activit is not as expected", MainActivity.class);

            Instrumentation.ActivityMonitor monitor = instrumentation
                    .addMonitor(activityClass.getName(), null, false);
            mainSolo.clickOnActionBarItem(R.id.actionbar_settings);

            Activity activityToBeClicked = instrumentation
                    .waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("not switched to next activity", activityToBeClicked);
            Solo clickedSolo = new Solo(instrumentation, activityToBeClicked);
            clickedSolo.assertCurrentActivity(
                    "current acitvity is not as expected", activityClass);

            monitor = instrumentation.addMonitor(MainActivity.class.getName(),
                    null, false);
            clickedSolo.getCurrentActivity().finish();
            Activity main = instrumentation.waitForMonitorWithTimeout(monitor,
                    5000);
            assertNotNull("not switched to main activty", main);

            mainSolo.assertCurrentActivity("wrong activity", MainActivity.class);
        } catch (Exception e)
        {
            assertTrue(false);
        }
    }


    private Solo clickActivity(Class<?> activity)
    {
        Instrumentation instrumentation = getInstrumentation();

        Solo mainSolo = new Solo(instrumentation, getActivity());
        mainSolo.assertCurrentActivity("current activit is not as expected",
                MainActivity.class);

        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(
                activity.getName(), null, false);
        mainSolo.clickOnActionBarItem(R.id.actionbar_settings);

        Activity activityToBeClicked = instrumentation
                .waitForMonitorWithTimeout(monitor, 5000);
        assertNotNull("not switched to next activity", activityToBeClicked);
        Solo clickedSolo = new Solo(instrumentation, activityToBeClicked);
        clickedSolo.assertCurrentActivity(
                "current acitvity is not as expected", activity);
        return clickedSolo;
    }


    public void testCheckboxDisabledIfPasswordEmpty()
    {
        Solo preferencesSolo = clickActivity(PreferencesActivity.class);
        setUsernamePasswort(preferencesSolo, "foo", "");
        preferencesSolo.getCurrentActivity().finish();
        preferencesSolo = clickActivity(PreferencesActivity.class);
        assertFalse(preferencesSolo.isCheckBoxChecked(0));
    }

    public void testCheckboxDisabledIfUsernameEmpty()
    {
        Solo preferencesSolo = clickActivity(PreferencesActivity.class);
        setUsernamePasswort(preferencesSolo, "foo", "");
        preferencesSolo.getCurrentActivity().finish();
        preferencesSolo = clickActivity(PreferencesActivity.class);
        assertFalse(preferencesSolo.isCheckBoxChecked(0));
    }
    
    public void testCheckboxDisabledIfNoCredentials()
    {
        Solo preferencesSolo = clickActivity(PreferencesActivity.class);
        setUsernamePasswort(preferencesSolo, "", "bar");
        preferencesSolo.getCurrentActivity().finish();
        preferencesSolo = clickActivity(PreferencesActivity.class);
        assertFalse(preferencesSolo.isCheckBoxChecked(0));
    }
    
    public void testCheckboxEnabled()
    {
        Solo preferencesSolo = clickActivity(PreferencesActivity.class);
        setUsernamePasswort(preferencesSolo, "foo", "bar");
        preferencesSolo.getCurrentActivity().finish();
        preferencesSolo = clickActivity(PreferencesActivity.class);
        assertTrue(preferencesSolo.isCheckBoxChecked(0));
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


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        log(getName() + ".setUp()");
    }


    @Override
    protected void tearDown() throws Exception
    {
        log(getName() + ".tearDown()");
        super.tearDown();
    }


    protected void log(final String m)
    {
        mLog.verbose(m);
    }

}
