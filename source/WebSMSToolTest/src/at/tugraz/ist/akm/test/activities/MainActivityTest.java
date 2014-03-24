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

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{
    private Intent mSmsServiceIntent = null;
    private Context mContext = null;

    private LogClient mLog = null;


    public MainActivityTest()
    {
        super("at.tugraz.ist.akm", MainActivity.class);
        TraceService.setSink(new ThrowingLogSink());
        mLog = new LogClient(MainActivityTest.class.getName());
    }


    /**
     * just show that the main activity starts without crashing
     */
    public void testMainActivityStart() throws Exception
    {
        MainActivity activity = getActivity();
        assertTrue(null != activity);
    }


    public void testStartStopButton() throws InterruptedException
    {
        SharedPreferencesProvider prefs = new SharedPreferencesProvider(getActivity()
                .getApplicationContext());
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();

        appKeystore.deleteKeystore(prefs.getKeyStoreFilePath());
        appKeystore.loadKeystore(prefs.getKeyStorePassword(), prefs.getKeyStoreFilePath());
        appKeystore.close();

        Solo solo = new Solo(getInstrumentation(), getActivity());
        solo.assertCurrentActivity("Current activty is not MainActivity",
                MainActivity.class);
        WifiManager wm = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);

        ToggleButton startStop = (ToggleButton) getActivity().findViewById(
                R.id.start_stop_server);
        startStop.setActivated(false);
        stopWebService();
        assertFalse(startStop.isChecked());

        solo.clickOnView(startStop);
        waitForServiceBeingStarted();

        if (wm.isWifiEnabled() || getActivity().isRunningOnEmulator())
        {
            assertTrue(startStop.isChecked());

            solo.clickOnView(startStop);
            waitForServiceBeingStopped();
            assertFalse(startStop.isChecked());
            stopWebService();
        } else
        {
            assertFalse(startStop.isChecked());

            solo.clickOnView(startStop);
            waitForServiceBeingStopped();
            assertFalse(startStop.isChecked());
            stopWebService();
        }
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        log(getName() + ".setUp()");
        mContext = getInstrumentation().getContext();
        mSmsServiceIntent = new Intent(mContext, WebSMSToolService.class);
    }


    @Override
    protected void tearDown() throws Exception
    {
        Solo s = new Solo(getInstrumentation());
        s.finishOpenedActivities();
        log(getName() + ".tearDown()");
        super.tearDown();
    }


    protected void log(final String m)
    {
        mLog.info(m);
    }


    public void startWebService()
    {
        mLog.info("Going to start web service");
        mContext.startService(mSmsServiceIntent);
        waitForServiceBeingStarted();
    }


    public void stopWebService()
    {
        mLog.info("Going to stop web service");
        mContext.stopService(mSmsServiceIntent);
        waitForServiceBeingStopped();
    }


    private boolean isWebServiceRunning()
    {
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext())
        {
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i
                    .next();
            mLog.debug("found service ["
                    + runningServiceInfo.service.getClassName() + "]");
            if (runningServiceInfo.service.getClassName().equals(
                    WebSMSToolService.class.getName()))
            {
                serviceRunning = true;
            }
        }
        return serviceRunning;
    }


    private void waitForServiceBeingStopped()
    {
        int maxTries = 20, delay = 200;
        mLog.debug("waitForServiceBeingStopped");
        try
        {
            this.wait(2000);
            while (isWebServiceRunning() && (maxTries-- > 0))
            {
                this.wait(delay);
                mLog.debug("waiting ...");
            }
        } catch (Exception ex)
        {
            // i don't care
        }
        mLog.debug("service has been stopped");
    }


    private void waitForServiceBeingStarted()
    {
        int maxTries = 20, delay = 200;
        mLog.debug("waitForServiceBeingStarted");
        try
        {
            this.wait(5000);
            while ((!isWebServiceRunning()) && (maxTries-- > 0))
            {
                this.wait(delay);
                mLog.debug("waiting ...");
            }
        } catch (Exception ex)
        {
            // i don't care
        }
        mLog.debug("service is running");
    }

}
