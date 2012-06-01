package at.tugraz.ist.akm;

import android.os.Bundle;
import android.view.MenuItem;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;


public class AboutActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
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
