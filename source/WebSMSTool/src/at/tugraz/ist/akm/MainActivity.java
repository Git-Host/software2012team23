package at.tugraz.ist.akm;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.trace.Logable;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends ActionBarActivity 
{
	private Intent mSmsServiceIntent = null;
	private Logable mLog = new Logable(getClass().getSimpleName());

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
            public void onClick(View view) {
                if (mSmsServiceIntent == null) {
                    mLog.logVerbose("Going to start web service");
                    
                    mSmsServiceIntent = new Intent(view.getContext(), WebSMSToolService.class);
                    view.getContext().startService(mSmsServiceIntent);
                    
                    Config config = new Config(getApplicationContext());
                    
                    String wifiIp  = getLocalIpAddress();
                    mLog.logDebug("Actual IP: " + wifiIp);
                    
                    
                    TextView ipFieldView =  (TextView) findViewById(R.id.adress_data_field);
                    ipFieldView.setText(config.getProtocol() + "://" + wifiIp + ":" +
                            config.getPort());
                } else {
                    view.getContext().stopService(mSmsServiceIntent);
                    mSmsServiceIntent = null;
                    TextView ipFieldView =  (TextView) findViewById(R.id.adress_data_field);
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
