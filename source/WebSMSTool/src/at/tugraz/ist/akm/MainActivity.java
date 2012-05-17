package at.tugraz.ist.akm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
		
		final ImageButton button = (ImageButton) findViewById(R.id.start_stop_server);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSmsServiceIntent == null) {
                    Log.v("Activity", "Going to start web service");
                    mSmsServiceIntent = new Intent(v.getContext(), WebSMSToolService.class);
                    v.getContext().startService(mSmsServiceIntent);
                    
                    button.setBackgroundResource(R.drawable.stop);
                } else {
                    v.getContext().stopService(mSmsServiceIntent);
                    mSmsServiceIntent = null;
                    
                    button.setBackgroundResource(R.drawable.start);
                }
            }
        });
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
}
