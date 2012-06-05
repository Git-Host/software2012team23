package at.tugraz.ist.akm;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;


public class AboutActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        TextView link = (TextView) findViewById(R.id.aboutInfoLink);
        Linkify.addLinks(link, Linkify.ALL);
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
}
