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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.PreferencesActivity;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;

import com.jayway.android.robotium.solo.Solo;

public class PreferencesActivityTest extends
        ActivityInstrumentationTestCase2<PreferencesActivity> implements OnSharedPreferenceChangeListener
{

    private LogClient mLog = null;
    private SharedPreferences mSharedPreferences = null;
    private int mOutstandingSaredPreferencesCallbacks = 0;

    public PreferencesActivityTest()
    {
        super(PreferencesActivity.class);
        TraceService.setSink(new ThrowingLogSink());
        mLog = new LogClient(PreferencesActivityTest.class.getName());
    }


    public void testOverallSettigns()
    {
        String outUsername = "username";
        String outPassword = "secret";
        String outPortNumber = "1";
//        String expectedInPort ="1024";
//        String outProtocolName = "http";
        
        Editor spEdit = mSharedPreferences.edit();
        spEdit.clear();
        spEdit.commit();
        spEdit.putString(resourceString(R.string.preferences_username_key), outUsername);
        spEdit.commit();
        spEdit.putString(resourceString(R.string.preferences_password_key), outPassword);
        spEdit.commit();
        mOutstandingSaredPreferencesCallbacks = 2;
        spEdit.putString(resourceString(R.string.preferences_server_port_key), outPortNumber);
        spEdit.commit();
        busyWaitForOutstandingCallsToBeFinished();
        
        assert(false);
        /*
        spEdit.putString(resourceString(R.string.preferences_server_protocol_key), outProtocolName);
        spEdit.commit();
        
        String inUsername = mSharedPreferences.getString(resourceString(R.string.preferences_username_key), "");
        String inPassword = mSharedPreferences.getString(resourceString(R.string.preferences_password_key), "");
        String inPortNumber = mSharedPreferences.getString(resourceString(R.string.preferences_server_port_key), "");
        String inProtocolName = mSharedPreferences.getString(resourceString(R.string.preferences_server_protocol_key), "");
  
        assertEquals(outUsername, inUsername);
        assertEquals(outPassword, inPassword);
        assertEquals(expectedInPort, inPortNumber);
        assertEquals(outProtocolName, inProtocolName);
        */
    }
    
    public void testProctocolValues()
    {
        try {
            mOutstandingSaredPreferencesCallbacks = 0;
            assertExpectedProtocolValues("http", "http");
            mOutstandingSaredPreferencesCallbacks = 0;
            assertExpectedProtocolValues("https", "https");
            mOutstandingSaredPreferencesCallbacks = 2;
            assertExpectedProtocolValues("httpf", "https");
            mOutstandingSaredPreferencesCallbacks = 2;
            assertExpectedProtocolValues("xxo", "https");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void testPortValues()
    {
        try
        {
            mOutstandingSaredPreferencesCallbacks = 2;
            assertExpectedPortValues(1, 1024);   
            mOutstandingSaredPreferencesCallbacks = 2;
            assertExpectedPortValues(1023, 1024);
            mOutstandingSaredPreferencesCallbacks = 0;
            assertExpectedPortValues(1024, 1024);
            mOutstandingSaredPreferencesCallbacks = 0;
            assertExpectedPortValues(1024, 1024);
            mOutstandingSaredPreferencesCallbacks = 0;
            assertExpectedPortValues(65535, 65535);
            mOutstandingSaredPreferencesCallbacks = 2;
            assertExpectedPortValues(65536, 65535);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void assertExpectedProtocolValues(String outProto, String expectedProto) throws Exception
    {
        assert(false);
        /*
        Editor spEdit = mSharedPreferences.edit();
        String serverProtocolResourceKey = resourceString(R.string.preferences_server_protocol_key); 
        
        spEdit.putString(serverProtocolResourceKey, outProto);
        spEdit.commit();
        busyWaitForOutstandingCallsToBeFinished();
        
        String inProto = mSharedPreferences.getString(serverProtocolResourceKey, "");  
        assertEquals(expectedProto, inProto);
        */
    }
    
    
    private void busyWaitForOutstandingCallsToBeFinished()
    {
        while ( mOutstandingSaredPreferencesCallbacks > 0);
    }
    
    public void assertExpectedPortValues(int outPortNumber, int expectedInPort) throws Exception
    {
        Editor spEdit = mSharedPreferences.edit();
        String serverPortResourceKey = resourceString(R.string.preferences_server_port_key); 
        
        spEdit.putString(serverPortResourceKey, Integer.toString(outPortNumber));
        spEdit.commit();
        while ( mOutstandingSaredPreferencesCallbacks > 0);
        
        String inPortNumber = mSharedPreferences.getString(serverPortResourceKey, "");  
        assertEquals(Integer.toString(expectedInPort), inPortNumber);
    }


    private String resourceString(int resourceStringId)
    {
        return getActivity().getApplicationContext().getResources()
                .getString(resourceStringId);
    }
    

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity()
                        .getApplicationContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mLog.debug(getName() + ".setUp()");
        
    }


    @Override
    protected void tearDown() throws Exception
    {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        Solo s = new Solo(getInstrumentation());
        s.finishOpenedActivities();
        mLog.debug(getName() + ".tearDown()");
        super.tearDown();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
    {
        if ( mOutstandingSaredPreferencesCallbacks > 0) 
            mOutstandingSaredPreferencesCallbacks--;
    }
}
