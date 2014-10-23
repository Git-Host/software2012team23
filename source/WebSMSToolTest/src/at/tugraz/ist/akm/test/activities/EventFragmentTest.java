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
import java.util.Date;
import java.util.LinkedList;

import android.app.Fragment;
import android.graphics.Point;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.EventFragment;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.test.trace.ui.TestableUiEvent;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.trace.ui.LoginEvent;
import at.tugraz.ist.akm.trace.ui.MessageEvent;
import at.tugraz.ist.akm.trace.ui.ResourceStringLoader;
import at.tugraz.ist.akm.trace.ui.ServiceEvent;
import at.tugraz.ist.akm.trace.ui.SettingsChangedEvent;
import at.tugraz.ist.akm.trace.ui.UiEvent;

import com.robotium.solo.Solo;

public class EventFragmentTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{

    private LogClient mLog = new LogClient(
            EventFragmentTest.class.getCanonicalName());
    private long mMessageWaitDelayMs = 150;


    public EventFragmentTest()
    {
        super(MainActivity.class);
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    private Fragment bring_fragment_on_top_using_NavigationDrawer(
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

        mLog.debug("brought fragment [" + otherFragmentTag + "] to front");
        return solo.getCurrentActivity().getFragmentManager()
                .findFragmentByTag(otherFragmentTag);
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


    public void test_send_MessageEvent_to_EventLogFragment()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        String m1Address = "0123888836", m1Body = "text asdf text";
        String m2Address = "001238888366", m2Body = "asdf text adsf";

        TextMessage message1 = new TextMessage();
        message1.setAddress(m1Address);
        message1.setBody(m1Body);
        TextMessage message2 = new TextMessage();
        message2.setAddress(m2Address);
        message2.setBody(m2Body);

        ResourceStringLoader stringLoader = new ResourceStringLoader(
                getActivity().getApplicationContext());
        final MessageEvent messageEvent1 = new MessageEvent(true);
        waitMsecs(mMessageWaitDelayMs);
        final MessageEvent messageEvent2 = new MessageEvent(false);
        messageEvent1.load(stringLoader, message1);
        messageEvent2.load(stringLoader, message2);

        try
        {
            stringLoader.close();
        }
        catch (IOException e)
        {
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                fragment.info(messageEvent2);
                fragment.info(messageEvent1);
            }
        });

        waitMsecs(mMessageWaitDelayMs);
        Solo solo = new Solo(getInstrumentation(), getActivity());

        assertEquals(true, solo.getCurrentActivity().getResources()
                .getDrawable(messageEvent1.getDrawableIconId()).isVisible());
        assertEquals(messageEvent1.getDate(), solo.getText(0).getText());
        assertEquals(messageEvent1.getTitle(), solo.getText(1).getText());
        assertEquals(messageEvent1.getDescription(), solo.getText(2).getText());
        assertEquals(messageEvent1.getTime(), solo.getText(3).getText());
        assertEquals(messageEvent1.getDetail(), solo.getText(4).getText());

        assertEquals(true, solo.getCurrentActivity().getResources()
                .getDrawable(messageEvent2.getDrawableIconId()).isVisible());
        assertEquals(messageEvent2.getDate(), solo.getText(5).getText());
        assertEquals(messageEvent2.getTitle(), solo.getText(6).getText());
        assertEquals(messageEvent2.getDescription(), solo.getText(7).getText());
        assertEquals(messageEvent2.getTime(), solo.getText(8).getText());
        assertEquals(messageEvent2.getDetail(), solo.getText(9).getText());
    }


    private void send2simpleEventsToEventLogFragment(final UiEvent event1,
            final UiEvent event2, final EventFragment logFragment)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                logFragment.info(event2);
                logFragment.info(event1);
            }
        });

        waitMsecs(mMessageWaitDelayMs);
        Solo solo = new Solo(getInstrumentation(), getActivity());

        assertEquals(true, solo.getCurrentActivity().getResources()
                .getDrawable(event1.getDrawableIconId()).isVisible());
        assertEquals(event1.getDate(), solo.getText(0).getText());
        assertEquals(event1.getTitle(), solo.getText(1).getText());
        assertEquals(event1.getTime(), solo.getText(3).getText());

        if (null == event1.getDescription())
        {
            assertEquals("", solo.getText(2).getText());
        } else
        {
            assertEquals(event1.getDescription(), solo.getText(2).getText());
        }

        if (null == event1.getDetail())
        {
            assertEquals("", solo.getText(4).getText());
        } else
        {
            assertEquals(event1.getDetail(), solo.getText(4).getText());
        }

        assertEquals(true, solo.getCurrentActivity().getResources()
                .getDrawable(event2.getDrawableIconId()).isVisible());
        assertEquals(event2.getDate(), solo.getText(5).getText());
        assertEquals(event2.getTitle(), solo.getText(6).getText());
        assertEquals(event2.getTime(), solo.getText(8).getText());

        if (null == event2.getDescription())
        {
            assertEquals("", solo.getText(7).getText());
        } else
        {
            assertEquals(event2.getDescription(), solo.getText(7).getText());
        }

        if (null == event2.getDetail())
        {
            assertEquals("", solo.getText(9).getText());
        } else
        {
            assertEquals(event2.getDetail(), solo.getText(9).getText());
        }

    }


    public void test_send_LoginEvent_to_EventLogFragment()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        ResourceStringLoader stringLoader = new ResourceStringLoader(
                getActivity().getApplicationContext());
        final LoginEvent event1 = new LoginEvent(true, "testuser");
        waitMsecs(mMessageWaitDelayMs);
        final LoginEvent event2 = new LoginEvent(false, "testuser2");
        event1.load(stringLoader);
        event2.load(stringLoader);

        try
        {
            stringLoader.close();
        }
        catch (IOException e)
        {
        }

        send2simpleEventsToEventLogFragment(event1, event2, fragment);
    }


    private void generateAdditionalEventLogs(LinkedList<UiEvent> eventBuffer,
            int eventsCount)
    {
        Date startDate = new Date();
        for (int count = 0; count < eventsCount; count++)
        {
            TestableUiEvent e = new TestableUiEvent(new Date(
                    startDate.getTime() + (1000 * count)));

            e.mTestTitle = "title[" + count + "]";
            e.mTestDescription = "desc[" + count + "]";
            e.mTestDetail = "detail[" + count + "]";
            eventBuffer.addFirst(e);
        }
    }


    public void test_send_ServiceEvent_list_and_check_chronological_insert_sort()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        int eventsCount = 10;
        final LinkedList<UiEvent> eventBuffer = new LinkedList<UiEvent>();
        generateAdditionalEventLogs(eventBuffer, eventsCount);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                fragment.info(eventBuffer);
            }
        });

        waitMsecs(mMessageWaitDelayMs);

        ListView listView = (ListView) getActivity().findViewById(
                R.id.event_list_list);
        assertTrue(listView.getChildCount() == eventsCount);

        for (int count = 0; count < eventsCount; count++)
        {
            TextView time = (TextView) listView.getChildAt(count).findViewById(
                    R.id.event_list_time);
            String formattedTime = time.getText().toString();
            String bufferedFormattedTime = eventBuffer.get(count).getTime();
            assertEquals(bufferedFormattedTime, formattedTime);
        }
    }


    public void test_send_ServiceEvent_list_plus_single_event_and_check_chronological_insert_sort()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        int eventsCount = 5;
        final LinkedList<UiEvent> eventBuffer = new LinkedList<UiEvent>();
        generateAdditionalEventLogs(eventBuffer, eventsCount);

        // single event
        waitMsecs(mMessageWaitDelayMs);
        Date currentTime = new Date();
        TestableUiEvent event = new TestableUiEvent(currentTime);

        event.mTestTitle = "title[xx]";
        event.mTestDescription = "desc[xx]";
        event.mTestDetail = "detail[xx]";
        eventBuffer.addFirst(event);

        waitMsecs(mMessageWaitDelayMs);
        generateAdditionalEventLogs(eventBuffer, eventsCount);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                fragment.info(eventBuffer);
            }
        });

        waitMsecs(mMessageWaitDelayMs);

        ListView listView = (ListView) getActivity().findViewById(
                R.id.event_list_list);
        assertEquals(listView.getChildCount(), (eventsCount + 1 + eventsCount));

        for (int count = 0; count < eventsCount; count++)
        {
            TextView time = (TextView) listView.getChildAt(count).findViewById(
                    R.id.event_list_time);
            String formattedTime = time.getText().toString();
            String bufferedFormattedTime = eventBuffer.get(count).getTime();
            assertEquals(bufferedFormattedTime, formattedTime);
        }

        // check single event
        TextView timeView = (TextView) listView.getChildAt(eventsCount + 1)
                .findViewById(R.id.event_list_time);
        String fTime = timeView.getText().toString();
        String bufferedTime = eventBuffer.get(eventsCount + 1).getTime();
        assertEquals(bufferedTime, fTime);

        for (int count = (eventsCount + 1); count < (eventsCount + 1 + eventsCount); count++)
        {
            TextView time = (TextView) listView.getChildAt(count).findViewById(
                    R.id.event_list_time);
            String formattedTime = time.getText().toString();
            String bufferedFormattedTime = eventBuffer.get(count).getTime();
            assertEquals(bufferedFormattedTime, formattedTime);
        }
    }


    public void test_send_ServiceEvent_to_EventLogFragment()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        ResourceStringLoader stringLoader = new ResourceStringLoader(
                getActivity().getApplicationContext());

        final ServiceEvent event1 = new ServiceEvent(true);
        waitMsecs(mMessageWaitDelayMs);
        final ServiceEvent event2 = new ServiceEvent(false);
        event1.load(stringLoader);
        event2.load(stringLoader);
        try
        {
            stringLoader.close();
        }
        catch (IOException e)
        {
        }
        send2simpleEventsToEventLogFragment(event1, event2, fragment);
    }


    public void test_send_SettingsChangedEvent_to_EventLogFragment()
    {
        final EventFragment fragment = (EventFragment) bring_fragment_on_top_using_NavigationDrawer(2);
        assertNotNull(fragment);

        ResourceStringLoader stringLoader = new ResourceStringLoader(
                getActivity().getApplicationContext());

        final SettingsChangedEvent event1 = new SettingsChangedEvent();
        waitMsecs(mMessageWaitDelayMs);
        final SettingsChangedEvent event2 = new SettingsChangedEvent();
        event1.load(stringLoader);
        event2.load(stringLoader);
        try
        {
            stringLoader.close();
        }
        catch (IOException e)
        {
        }
        send2simpleEventsToEventLogFragment(event1, event2, fragment);
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }


    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }


    private void waitMsecs(long msecs)
    {
        synchronized (this)
        {
            try
            {
                wait(msecs);
            }
            catch (InterruptedException e)
            {

            }
        }
    }
}
