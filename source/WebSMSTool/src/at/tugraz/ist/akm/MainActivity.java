package at.tugraz.ist.akm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends ActionBarActivity 
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    
	    // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.settings:
	            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
	            MainActivity.this.startActivity(myIntent);
	            break;
	    }
	    
	    return super.onOptionsItemSelected(item);
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
