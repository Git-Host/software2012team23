package at.tugraz.ist.akm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends Activity
{

	private Intent mSmsServiceIntent = null;

	/**
	 * starts the main service (web server etc.)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.v("Activity", "Going to start web service");
		mSmsServiceIntent = new Intent(this, WebSMSToolService.class);
		this.startService(mSmsServiceIntent);
	}

	/**
	 * stops underlying service if running
	 */
	public void stopService()
	{
		if (null != mSmsServiceIntent)
		{
			this.stopService(mSmsServiceIntent);
		}
	}
}
