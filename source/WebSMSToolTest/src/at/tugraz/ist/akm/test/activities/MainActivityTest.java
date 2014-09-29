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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.activities.StartServiceFragment;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

import com.robotium.solo.Solo;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{
    private Intent mSmsServiceIntent = null;
    private Context mContext = null;

    private LogClient mLog = null;


    public MainActivityTest()
    {
        super(MainActivity.class);
        TraceService.setSink(new ThrowingLogSink());
        mLog = new LogClient(MainActivityTest.class.getName());
    }


    public void testMainActivityStart() throws Exception
    {
        MainActivity activity = getActivity();
        assertTrue(null != activity);
    }


    public void testStartStopButton() throws InterruptedException
    {

        SharedPreferencesProvider prefs = new SharedPreferencesProvider(
                getActivity().getApplicationContext());
        ApplicationKeyStore appKeystore = new ApplicationKeyStore();

        appKeystore.deleteKeystore(prefs.getKeyStoreFilePath());
        appKeystore.loadKeystore(prefs.getKeyStorePassword(),
                prefs.getKeyStoreFilePath());
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

        sleepdMs(800);
        assertFalse(startStop.isChecked());

        solo.clickOnView(startStop);
        waitForServiceBeingStarted();

        StartServiceFragment fragment = new StartServiceFragment();
        if (wm.isWifiEnabled() || fragment.isRunningOnEmulator())
        {
            sleepdMs(800);
            assertTrue(startStop.isChecked());

            solo.clickOnView(startStop);
            waitForServiceBeingStopped();
            sleepdMs(800);
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


    public void test_backStack_homeToHome()
    {
        assertTrue(false);
    }


    private void bringMainFragmentOnTop_thenBringOhterFragmentOnTop_thenBringMainFragmentOnTop_usingBackstack_and_NavigationDrawer(
            int otherFragmentIdxInNavigationDrawer)
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());

        dragNavigationMenu();

        solo.waitForFragmentByTag(getFragmentOfNavigationDrawerMenu(0));
        solo.clickOnView(findNavigationDrawerMenu(otherFragmentIdxInNavigationDrawer));
        solo.waitForFragmentByTag(getFragmentOfNavigationDrawerMenu(otherFragmentIdxInNavigationDrawer));
        assertFragmentVisible(
                true,
                getFragmentOfNavigationDrawerMenu(otherFragmentIdxInNavigationDrawer));
        assertFragmentVisible(false, getFragmentOfNavigationDrawerMenu(0));
        solo.sendKey(KeyEvent.KEYCODE_BACK);

        dragNavigationMenu();
        solo.clickOnView(findNavigationDrawerMenu(0));
        solo.waitForFragmentByTag(getFragmentOfNavigationDrawerMenu(0));
        assertFragmentVisible(true, getFragmentOfNavigationDrawerMenu(0));
        assertFragmentVisible(
                false,
                getFragmentOfNavigationDrawerMenu(otherFragmentIdxInNavigationDrawer));
    }


    private void assertFragmentVisible(boolean visible, String viewTag)
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());
        Fragment view = solo.getCurrentActivity().getFragmentManager()
                .findFragmentByTag(viewTag);

        if (visible)
        {
            assertTrue(view != null);

        } else
        {
            if (view != null)
            {
                assertFalse(view.isVisible());
            } else
            {
                assertTrue(view == null);
            }
        }
    }


    private void dragNavigationMenu()
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        solo.drag(0, size.y / 2, size.x / 2, size.x / 2, 10);
    }


    private String getFragmentOfNavigationDrawerMenu(int idx)
    {
        return getActivity().getResources().getStringArray(
                R.array.drawer_fragment_array)[idx];
    }


    private String getNameOfNavigationDrawerMenu(int idx)
    {
        return getActivity().getResources().getStringArray(
                R.array.drawer_string_array)[idx];
    }


    private View findNavigationDrawerMenu(int id)
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());
        try
        {
            View menuEntry = solo.getText(getNameOfNavigationDrawerMenu(id));
            return menuEntry;
        }
        catch (Throwable e)
        {
            return null;
        }
    }


    private void logViews(ArrayList<View> views)
    {
        int numIdLessViews = 0;
        int numViews = 0;

        for (View v : views)
        {
            String name = "-";
            numViews++;
            try
            {
                name = getActivity().getResources().getResourceName(v.getId());
                mLog.debug("*[" + name + "][" + v.getId() + "] ");
            }
            catch (Throwable e)
            {
                numIdLessViews++;
            }
        }
        mLog.debug("found [" + numViews + "] and suppressed [" + numIdLessViews
                + "] views");
    }


    public void test_backStack_messagesToHome()
    {
        bringMainFragmentOnTop_thenBringOhterFragmentOnTop_thenBringMainFragmentOnTop_usingBackstack_and_NavigationDrawer(1);
    }


    public void test_backStack_settingsToHome()
    {
        bringMainFragmentOnTop_thenBringOhterFragmentOnTop_thenBringMainFragmentOnTop_usingBackstack_and_NavigationDrawer(2);
    }


    public void test_backStack_aboutToHome()
    {
        bringMainFragmentOnTop_thenBringOhterFragmentOnTop_thenBringMainFragmentOnTop_usingBackstack_and_NavigationDrawer(3);
    }


    public void test_backStack_multipletimesSettingsKlickedToHome()
    {
        assertTrue(false);
    }


    private void sleepdMs(long msecs)
    {

        synchronized (this)
        {
            try
            {

                wait(msecs);
            }
            catch (InterruptedException e)
            {
                // don't care
            }
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
        }
        catch (Exception ex)
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
        }
        catch (Exception ex)
        {
            // i don't care
        }
        mLog.debug("service is running");
    }

}
