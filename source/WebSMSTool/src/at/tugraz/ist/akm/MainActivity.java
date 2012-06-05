package at.tugraz.ist.akm;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
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
		
		final ToggleButton button = (ToggleButton) findViewById(R.id.start_stop_server);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSmsServiceIntent == null) {
                    Log.v("Activity", "Going to start web service");
                    
                    button.setVisibility(1);
                    
                    mSmsServiceIntent = new Intent(v.getContext(), WebSMSToolService.class);
                    v.getContext().startService(mSmsServiceIntent);
                    
                    button.setVisibility(0);
                    
                    String wifiIp  = getLocalIpAddress();
                    Log.d("Actual IP:", wifiIp);
                    
                    
                    TextView ipFieldView =  (TextView) findViewById(R.id.ip_data_field);
                    ipFieldView.setText(getString(R.string.ipTitle)+wifiIp);
                } else {
                    v.getContext().stopService(mSmsServiceIntent);
                    mSmsServiceIntent = null;
                    TextView ipFieldView =  (TextView) findViewById(R.id.ip_data_field);
                    ipFieldView.setText("");
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
	            
	        case R.id.info:
	            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
	            MainActivity.this.startActivity(aboutIntent);
	            break; 
	    }
	    
	    
	    return super.onOptionsItemSelected(item);
	}
	
	
	
	public String getLocalIpAddress() {
	    /*
	     //Lookup in all network interfaces
	     try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("GetLocalIpAddress", ex.toString());
	    }*/
	    
	    //the direct wifi way
	    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	    int ipAddress = wifiInfo.getIpAddress();
	    String ip = Formatter.formatIpAddress(ipAddress);
	    return ip;
	}
}
