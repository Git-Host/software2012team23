package at.tugraz.ist.akm.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivityTest extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(at.tugraz.ist.akm.test.R.layout.main);
        Log.v(MainActivityTest.class.getSimpleName(), "test activity started");
    }
}
