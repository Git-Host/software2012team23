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
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.SharedPreferencesProvider;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;

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
        TraceService.setSink(new ExceptionThrowingLogSink());
        mLog = new LogClient(MainActivityTest.class.getName());
    }


    public void test_MainActivityStart() throws Exception
    {
        MainActivity activity = getActivity();
        assertTrue(null != activity);
    }


    public void test_StartStopButton() throws InterruptedException
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

        waitMs(800);
        assertFalse(startStop.isChecked());

        solo.clickOnView(startStop);
        waitForServiceBeingStarted();

        if (wm.isWifiEnabled() || AppEnvironment.isRunningOnEmulator())
        {
            waitMs(800);
            assertTrue(startStop.isChecked());

            solo.clickOnView(startStop);
            waitForServiceBeingStopped();
            waitMs(800);
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


    public void test_backStack_multipleClicks_results_in_stack_size_max_2()
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());

        String mainFragmentTag = getFragmentOfNavigationDrawerMenu(0);
        String otherFragmentTag1 = getFragmentOfNavigationDrawerMenu(1);
        String otherFragmentTag2 = getFragmentOfNavigationDrawerMenu(2);
        String otherFragmentTag3 = getFragmentOfNavigationDrawerMenu(3);

        solo.waitForFragmentByTag(mainFragmentTag);

        dragToOpenNavigationMenu();
        solo.clickOnView(findNavigationDrawerMenuView(1));
        solo.waitForFragmentByTag(otherFragmentTag1);
        assertFragmentVisible(true, otherFragmentTag1);
        assertFragmentVisible(false, mainFragmentTag);

        dragToOpenNavigationMenu();
        solo.clickOnView(findNavigationDrawerMenuView(2));
        solo.waitForFragmentByTag(otherFragmentTag2);
        assertFragmentVisible(true, otherFragmentTag2);
        assertFragmentVisible(false, mainFragmentTag);

        dragToOpenNavigationMenu();
        solo.clickOnView(findNavigationDrawerMenuView(3));
        solo.waitForFragmentByTag(otherFragmentTag3);
        assertFragmentVisible(true, otherFragmentTag3);
        assertFragmentVisible(false, mainFragmentTag);
        solo.sendKey(KeyEvent.KEYCODE_BACK);

        solo.waitForFragmentByTag(mainFragmentTag);
        assertFragmentVisible(true, mainFragmentTag);
        assertFragmentVisible(false, otherFragmentTag1);
        assertFragmentVisible(false, otherFragmentTag2);
        assertFragmentVisible(false, otherFragmentTag3);
    }


    public void test_StartStopServiceFragment_portrait_landscape_portrait()
    {
        switchOrientationForNavigationDrawerFragment(0);
    }


    public void test_MessagesLog_portrait_landscape_portrait()
    {
        switchOrientationForNavigationDrawerFragment(1);
    }


    public void test_Preferences_portrait_landscape_portrait()
    {
        switchOrientationForNavigationDrawerFragment(2);
    }


    public void test_About_portrait_landscape_portrait()
    {
        switchOrientationForNavigationDrawerFragment(3);
    }


    public void switchOrientationForNavigationDrawerFragment(
            int navigationDrawerEntryIdx)
    {
        try
        {
            Solo solo = new Solo(getInstrumentation(), getActivity());

            String mainFragmentTag = getFragmentOfNavigationDrawerMenu(0);
            String otherFragmentTag = getFragmentOfNavigationDrawerMenu(navigationDrawerEntryIdx);

            solo.waitForFragmentByTag(mainFragmentTag);

            dragToOpenNavigationMenu();
            solo.clickOnView(findNavigationDrawerMenuView(navigationDrawerEntryIdx));
            solo.waitForFragmentByTag(otherFragmentTag);
            assertFragmentVisible(true, otherFragmentTag);

            solo.setActivityOrientation(Solo.PORTRAIT);
            getInstrumentation().waitForIdleSync();
            solo.setActivityOrientation(Solo.LANDSCAPE);
            getInstrumentation().waitForIdleSync();
            solo.setActivityOrientation(Solo.PORTRAIT);
            getInstrumentation().waitForIdleSync();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    private void bringMainFragment_thenOhterFragment_thenMainFragment_onTop_using_NavigationDrawer(
            int otherNavigationDrawerIdxIDx)
    {
        Solo solo = new Solo(getInstrumentation(), getActivity());

        String mainFragmentTag = getFragmentOfNavigationDrawerMenu(0);
        String otherFragmentTag = getFragmentOfNavigationDrawerMenu(otherNavigationDrawerIdxIDx);

        solo.waitForFragmentByTag(mainFragmentTag);

        dragToOpenNavigationMenu();
        solo.clickOnView(findNavigationDrawerMenuView(otherNavigationDrawerIdxIDx));
        solo.waitForFragmentByTag(otherFragmentTag);
        assertFragmentVisible(true, otherFragmentTag);
        assertFragmentVisible(false, mainFragmentTag);

        solo.sendKey(KeyEvent.KEYCODE_BACK);

        solo.waitForFragmentByTag(mainFragmentTag);
        assertFragmentVisible(true, mainFragmentTag);
        assertFragmentVisible(false, otherFragmentTag);
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


    private void dragToOpenNavigationMenu()
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


    private View findNavigationDrawerMenuView(int id)
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


    public void test_backStack_messagesToHome()
    {
        bringMainFragment_thenOhterFragment_thenMainFragment_onTop_using_NavigationDrawer(1);
    }


    public void test_backStack_settingsToHome()
    {
        bringMainFragment_thenOhterFragment_thenMainFragment_onTop_using_NavigationDrawer(2);
    }


    public void test_backStack_aboutToHome()
    {
        bringMainFragment_thenOhterFragment_thenMainFragment_onTop_using_NavigationDrawer(3);
    }


    public void test_backStack_homeClicks_result_in_only_one_stack_etnry()
    {
        try
        {
            Solo solo = new Solo(getInstrumentation(), getActivity());

            String mainFragmentTag = getFragmentOfNavigationDrawerMenu(0);

            solo.waitForFragmentByTag(mainFragmentTag);

            dragToOpenNavigationMenu();
            solo.clickOnView(findNavigationDrawerMenuView(0));
            solo.waitForFragmentByTag(mainFragmentTag);
            assertFragmentVisible(true, mainFragmentTag);

            dragToOpenNavigationMenu();
            solo.clickOnView(findNavigationDrawerMenuView(0));
            solo.waitForFragmentByTag(mainFragmentTag);
            assertFragmentVisible(true, mainFragmentTag);
            solo.sleep(1000);

            dragToOpenNavigationMenu();
            solo.clickOnView(findNavigationDrawerMenuView(0));
            solo.waitForFragmentByTag(mainFragmentTag);
            assertFragmentVisible(true, mainFragmentTag);

            solo.sendKey(KeyEvent.KEYCODE_BACK);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            assertTrue(false);
        }

    }


    private void waitMs(long msecs)
    {

        synchronized (this)
        {
            try
            {
                wait(msecs);
            }
            catch (InterruptedException e)
            {
                mLog.error("interrupted diring wait", e);
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
        log(getName() + ".tearDown()");
        Solo s = new Solo(getInstrumentation());
        s.finishOpenedActivities();
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
            // mLog.debug("found service ["
            // + runningServiceInfo.service.getClassName() + "]");
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

        int maxTries = 20;
        mLog.debug("waitForServiceBeingStopped");
        waitMs(2000);
        while (isWebServiceRunning() && (maxTries-- > 0))
        {
            waitMs(200);
            mLog.debug("waiting ...");
        }
        mLog.debug("service has been stopped");
    }


    private void waitForServiceBeingStarted()
    {
        int maxTries = 20, delay = 200;
        mLog.debug("waitForServiceBeingStarted");
        waitMs(5000);
        while ((!isWebServiceRunning()) && (maxTries-- > 0))
        {
            waitMs(delay);
            mLog.debug("waiting ...");
        }
        mLog.debug("service is running");
    }

}
