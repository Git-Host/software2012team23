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

package at.tugraz.ist.akm.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.SettingsActivity;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.test.trace.ThrowingLogSink;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.trace.Logger;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityActionBarTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Logable mLog = null;
	
	public MainActivityActionBarTest()
	{
		super("at.tugraz.ist.akm", MainActivity.class);
		Logger.setSink(new ThrowingLogSink());
		mLog = new Logable(MainActivityActionBarTest.class.getSimpleName());
	}


	public void testSettingsActivity() throws Exception {
	    Instrumentation instrumentation = getInstrumentation();
	    
	    Solo mainSolo = new Solo(instrumentation, getActivity());
		mainSolo.assertCurrentActivity("Actual activty is MainActivity", MainActivity.class);

		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(SettingsActivity.class.getName(), null, false);
		
		ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.actionbar_compat_item_settings);
		mainSolo.clickOnView(settingsBtn);
		
		Activity settings = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
	    assertNotNull("The actual activity should have switched", settings);
		
		Solo settingsSolo = new Solo(instrumentation, settings);
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", SettingsActivity.class);
		
        EditText username = (EditText) settings.findViewById(R.id.username);
        settingsSolo.clickOnView(username);
        settingsSolo.clearEditText(username);
        
        EditText password = (EditText) settings.findViewById(R.id.password);
        settingsSolo.clickOnView(password);
        settingsSolo.clearEditText(password);
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        EditText port = (EditText) settings.findViewById(R.id.port);
        settingsSolo.clickOnView(port);
        settingsSolo.clearEditText(port);
        instrumentation.sendStringSync("8887");
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        RadioButton http = (RadioButton) settings.findViewById(R.id.http);
        settingsSolo.clickOnView(http);
        
        Button save = (Button) settings.findViewById(R.id.savesettings);
        settingsSolo.clickOnView(save);
        
        Thread.sleep(100);
        
        Context context = instrumentation.getTargetContext().getApplicationContext();
        Config config = new Config(context);
        assertEquals("First save user should be empty.", "", config.getUserName());
        assertEquals("First save password should be empty.", "", config.getPassWord());
        assertEquals("First save port should be 8887.", "8887", config.getPort());
        assertEquals("First save protocol should http.", "http", config.getProtocol());
        
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", MainActivity.class);
        
        mainSolo.clickOnView(settingsBtn);
        
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", SettingsActivity.class);
        
        settingsSolo.clickOnView(username);
        instrumentation.sendStringSync("MyUsername");
        
        settingsSolo.clickOnView(password);
        instrumentation.sendStringSync("MyPassword");
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        settingsSolo.clickOnView(port);
        settingsSolo.clearEditText(port);
        instrumentation.sendStringSync("8998");
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        RadioButton https = (RadioButton) settings.findViewById(R.id.https);
        settingsSolo.clickOnView(https);
        settingsSolo.clickOnView(save);
        
        Thread.sleep(200);
        
        Config config2 = new Config(context);
        assertEquals("Second save user should not empty.", "MyUsername", config2.getUserName());
        assertEquals("Second save password should not empty.", "MyPassword", config2.getPassWord());
        assertEquals("Second save port should be 8998.", "8998", config2.getPort());
        assertEquals("Second save protocol should https.", "https", config2.getProtocol());
        
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", MainActivity.class);
	}
	
	public void testMandatoryPort() throws Exception {
        Instrumentation instrumentation = getInstrumentation();
        
        Solo mainSolo = new Solo(instrumentation, getActivity());
        mainSolo.assertCurrentActivity("Actual activty is MainActivity", MainActivity.class);

        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(SettingsActivity.class.getName(), null, false);
        
        ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.actionbar_compat_item_settings);
        mainSolo.clickOnView(settingsBtn);
        
        Activity settings = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
        assertNotNull("The actual activity should have switched", settings);
        
        Solo settingsSolo = new Solo(instrumentation, settings);
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", SettingsActivity.class);
        
        EditText port = (EditText) settings.findViewById(R.id.port);
        settingsSolo.clickOnView(port);
        settingsSolo.clearEditText(port);
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        Button save = (Button) settings.findViewById(R.id.savesettings);
        settingsSolo.clickOnView(save);
        
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", SettingsActivity.class);
        
        settingsSolo.clickOnView(port);
        instrumentation.sendStringSync("8887");
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        
        settingsSolo.clickOnView(save);
        
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", MainActivity.class);
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
		mLog.logVerbose(m);
	}

}
