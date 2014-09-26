package at.tugraz.ist.akm.activities.preferences;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.preferences.OnSharedPreferenceEventListenValidator;
import at.tugraz.ist.akm.trace.LogClient;

public class PrefsFragment extends PreferenceFragment
{
    private Context mApplicationContext = null;
    private LogClient mLog = new LogClient(
            PrefsFragment.class.getCanonicalName());

    private OnSharedPreferenceEventListenValidator mPreferenceChangedListener = null;


    public PrefsFragment()
    {
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_list);
    }


    private void registerListener()
    {
        mLog.debug("fragment register listener");
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext);
        sp.registerOnSharedPreferenceChangeListener(mPreferenceChangedListener);

        Map<String, ?> keys = sp.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet())
        {
            String p = entry.getKey();
            findPreference(p).setOnPreferenceClickListener(
                    mPreferenceChangedListener);
        }

    }


    private void unregisterListener()
    {
        mLog.debug("fragment unregiser listener");
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext);
        sp.unregisterOnSharedPreferenceChangeListener(mPreferenceChangedListener);

        Map<String, ?> keys = sp.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet())
        {
            String p = entry.getKey();
            findPreference(p).setOnPreferenceClickListener(null);
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        mLog.debug("fragment onStart()");

        mApplicationContext = getActivity();
        mPreferenceChangedListener = new OnSharedPreferenceEventListenValidator(
                this, mApplicationContext);
        mPreferenceChangedListener.updateSettingsOnPrefsView();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("fragment onResume()");
        registerListener();
    }


    @Override
    public void onPause()
    {
        super.onPause();
        mLog.debug("fragment onPause()");
        unregisterListener();
    }


    @Override
    public void onStop()
    {
        mLog.debug("fragment onStop()");
        super.onStop();
    }
}
