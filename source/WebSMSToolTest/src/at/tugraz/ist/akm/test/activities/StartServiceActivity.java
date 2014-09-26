package at.tugraz.ist.akm.test.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.StartServiceFragment;

public class StartServiceActivity extends Activity
{

    StartServiceFragment mFragment = new StartServiceFragment();
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().setIcon(
                getResources().getDrawable(R.drawable.ic_notification));


        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(android.R.id.content, mFragment);
        transaction.commit();
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }
    
    public boolean isRunningOnEmulator() {
        return mFragment.isRunningOnEmulator();
    }

}
