package at.tugraz.ist.akm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // it would be good practice to check whether the service not
        // running and then start it
        // Log.v("Activity", "Going to start web service");
        this.startService(new Intent(this, WebSMSToolService.class));
    }
}
