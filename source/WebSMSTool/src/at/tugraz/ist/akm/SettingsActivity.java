package at.tugraz.ist.akm;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;
import at.tugraz.ist.akm.content.Config;

public class SettingsActivity extends ActionBarActivity {
	EditText userName = null;
	EditText passWord = null;
	EditText port = null;
	RadioButton http = null;
	RadioButton https = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        userName = (EditText)findViewById(R.id.username);
        passWord = (EditText)findViewById(R.id.password);
        port = (EditText)findViewById(R.id.port);
        http = (RadioButton)findViewById(R.id.http);
        https = (RadioButton)findViewById(R.id.https);
        
        getSettings();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public void saveSettings(View view) {
        if (port.getText() == null || port.getText().toString().length() == 0) {
            port.setError("This field is mandatory, please add a value (default 8887).");
            return;
        }
        
        Config config = new Config(getApplicationContext());
    	
    	config.setUserName(userName.getText().toString());
    	config.setPassword(passWord.getText().toString());
    	    	
    	config.setPort(port.getText().toString());
    	if (http.isChecked()) {
    		config.setProtocol("http");
    	}
    	else {
    		config.setProtocol("https");
    	}
    	finish();
    }
    
    private void getSettings() {
    	Config config = new Config(getApplicationContext());
    	
    	userName.setText(config.getUserName());
    	passWord.setText(config.getPassWord());
    	port.setText(config.getPort());
    	if (config.getProtocol().equals("http")) {
    		http.setChecked(true);
    		https.setChecked(false);
    	}
    	else {
    		http.setChecked(false);
    		https.setChecked(true);
    	}
    }
}
