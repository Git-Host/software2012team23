package at.tugraz.ist.akm;

import android.os.Bundle;
import at.tugraz.ist.akm.actionbar.ActionBarActivity;


public class AboutActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
}
