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
		long fivesecs = 5000;
		
		
	    Instrumentation instrumentation = getInstrumentation();
	    
	    Solo mainSolo = new Solo(instrumentation, getActivity());
		mainSolo.assertCurrentActivity("Current activty is MainActivity", MainActivity.class);
		
		mainSolo.setActivityOrientation(Solo.LANDSCAPE);
		mainSolo.setActivityOrientation(Solo.PORTRAIT);
		mainSolo.setActivityOrientation(Solo.LANDSCAPE);
		
		Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(SettingsActivity.class.getName(), null, false);
		mainSolo.clickOnActionBarItem(R.id.actionbar_settings);
		
		Activity settings = instrumentation.waitForMonitorWithTimeout(monitor, fivesecs);
		
	    assertNotNull("Current activity should have switched", settings);
		Solo settingsSolo = new Solo(instrumentation, settings);
        settingsSolo.assertCurrentActivity("Current activty is SettingsActivity", SettingsActivity.class);
		
        EditText username = (EditText) settingsSolo.getView(R.id.username);
        EditText password = (EditText) settingsSolo.getView(R.id.password);
        EditText port = (EditText) settingsSolo.getView(R.id.port);
        RadioButton http = (RadioButton) settingsSolo.getView(R.id.http);
        RadioButton https = (RadioButton) settingsSolo.getView(R.id.https);
        Button save = (Button) settingsSolo.getView(R.id.savesettings);
       
        settingsSolo.clearEditText(username);
        settingsSolo.clearEditText(password);
        settingsSolo.clearEditText(port);
        
        settingsSolo.clickOnView(port);
        instrumentation.sendStringSync("8887");

        settingsSolo.clickOnView(http);
        settingsSolo.clickOnView(save);
        
        Thread.sleep(1000);
        Context context = instrumentation.getTargetContext().getApplicationContext();
        Config config = new Config(context);
        assertEquals("", config.getUserName());
        assertEquals("", config.getPassWord());
        assertEquals("8887", config.getPort());
        assertEquals("http", config.getProtocol());
        
        
        mainSolo.setActivityOrientation(Solo.LANDSCAPE);
		mainSolo.setActivityOrientation(Solo.PORTRAIT);
		mainSolo.setActivityOrientation(Solo.LANDSCAPE);
		
        settingsSolo.assertCurrentActivity("Current activty should be MainActivity", MainActivity.class);
        mainSolo = new Solo(instrumentation, getActivity());
        monitor = instrumentation.addMonitor(SettingsActivity.class.getName(), null, false);
		mainSolo.clickOnActionBarItem(R.id.actionbar_settings);
		settings = instrumentation.waitForMonitorWithTimeout(monitor, fivesecs);
	    assertNotNull("wrong activity", settings);
		settingsSolo = new Solo(instrumentation, settings);
        settingsSolo.assertCurrentActivity("Current activty should be SettingsActivity", SettingsActivity.class);
        
        username = (EditText) settingsSolo.getView(R.id.username);
        password = (EditText) settingsSolo.getView(R.id.password);
        port = (EditText) settingsSolo.getView(R.id.port);
        http = (RadioButton) settingsSolo.getView(R.id.http);
        https = (RadioButton) settingsSolo.getView(R.id.https);
        save = (Button) settingsSolo.getView(R.id.savesettings);
        
		settingsSolo.clearEditText(port);
	   
		settingsSolo.clickOnView(username);
		instrumentation.sendStringSync("MyUsername");
	   
		settingsSolo.clickOnView(password);
		instrumentation.sendStringSync("MyPassword");
	    
		settingsSolo.clickOnView(port);
		instrumentation.sendStringSync("8998");
	   
		settingsSolo.clickOnView(https);
		settingsSolo.clickOnView(save);        
		
		Thread.sleep(1000);
        Config config2 = new Config(context);
        assertEquals("MyUsername", config2.getUserName());
        assertEquals("MyPassword", config2.getPassWord());
        assertEquals("8998", config2.getPort());
        assertEquals("https", config2.getProtocol());
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_BACK);
        settingsSolo.assertCurrentActivity("Actual activty is SettingsActivity", MainActivity.class);
	}
	
	public void testMandatoryPort() throws Exception {
        Instrumentation instrumentation = getInstrumentation();
        
        Solo mainSolo = new Solo(instrumentation, getActivity());
        mainSolo.assertCurrentActivity("Actual activty is MainActivity", MainActivity.class);

        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(SettingsActivity.class.getName(), null, false);
        mainSolo.clickOnActionBarItem(R.id.actionbar_settings);
        
        Activity settings = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
        assertNotNull("not switched to settings activity", settings);
        
        Solo settingsSolo = new Solo(instrumentation, settings);
        settingsSolo.assertCurrentActivity("wrong activity", SettingsActivity.class);
        
        EditText port = (EditText) settings.findViewById(R.id.port);
        settingsSolo.clickOnView(port);
        settingsSolo.clearEditText(port);
        
        Button save = (Button) settings.findViewById(R.id.savesettings);
        settingsSolo.clickOnView(save);
        
        settingsSolo.assertCurrentActivity("wrong activity", SettingsActivity.class);
        
        settingsSolo.clickOnView(port);
        instrumentation.sendStringSync("8887");
        
        settingsSolo.sendKey(KeyEvent.KEYCODE_ENTER);
        
        settingsSolo.clickOnView(save);
        
        settingsSolo.assertCurrentActivity("wrong activity", MainActivity.class);
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
