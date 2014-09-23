package at.tugraz.ist.akm.activities.preferences;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import at.tugraz.ist.akm.R;

public class PreferencesActivity extends PreferenceActivity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().setIcon(
                getResources().getDrawable(R.drawable.ic_notification));

        PrefsFragment preferenceFragment = new PrefsFragment(
                getApplicationContext());
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(android.R.id.content, preferenceFragment);
        transaction.commit();
    }


    @Override
    protected void onStop()
    {
        // updateAccessRestrictionCheckboxDependingOnCredentials();
        super.onStop();
    }

}
