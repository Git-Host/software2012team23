package at.tugraz.ist.akm;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.trace.LogClient;

public class SettingsActivity2 extends PreferenceActivity implements
        OnSharedPreferenceChangeListener
{
    private static final String PREFERENCES_KEY_USERNAME = "preferences_username";
    private static final String PREFERENCES_KEY_PASSWORD = "preferences_password";
    private static final String PREFERENCES_KEY_SERVER_PORT= "preferences_server_port";
    private static final String PREFERENCES_KEY_SERVER_PROTOCOL= "preferences_server_protocol";

    LogClient mLog = new LogClient(this);


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_list);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setPreferenceSummary(preferences, PREFERENCES_KEY_USERNAME);
        setPreferenceSummary(preferences, PREFERENCES_KEY_PASSWORD);
        setPreferenceSummary(preferences, PREFERENCES_KEY_SERVER_PORT);
        setPreferenceSummary(preferences, PREFERENCES_KEY_SERVER_PROTOCOL);
    }


   
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        if ( key.equals(PREFERENCES_KEY_SERVER_PORT)) 
        {
            int enteredPortNumber = 0;
            try {
                enteredPortNumber = Integer.parseInt(sharedPreferences.getString(key, "8888"));
            }
            catch (NumberFormatException e) {
                enteredPortNumber = 8888;
                mLog.logWarning("ignoring invalid user input");
            }
            enteredPortNumber = portNumberTrimmer(enteredPortNumber);
            findPreference(key).setSummary(Integer.toString(enteredPortNumber));

            Editor ed = sharedPreferences.edit();
            ed.putString(key, Integer.toString(enteredPortNumber));       
            ed.apply();
        }
        setPreferenceSummary(sharedPreferences, key);
    }

    
    private int portNumberTrimmer(int uncheckedNumber) 
    {
        if (uncheckedNumber < 1024) 
            return 1024;
        else if ( uncheckedNumber > 65535)
            return 65535;
        else
            return uncheckedNumber;
    }
    
    
    private void setPreferenceSummary(SharedPreferences sharedPref, String key) 
    {
        try {
            Preference preferenceItem = findPreference(key);
            if ( null != preferenceItem ) 
            {
                String summary = new String(sharedPref.getString(key, ""));
                if ( key.equals(PREFERENCES_KEY_PASSWORD)) {
                    summary = getCoveredPassword(summary);
                }
                preferenceItem.setSummary(summary);
            }
        }
        catch(ClassCastException c) {
            mLog.logError("failed to set preference summary: " + c.getMessage());
        }
    }

    
    private String getCoveredPassword(final String plainPassword)
    {
        StringBuffer hiddenPassword = new StringBuffer(plainPassword.length());
        for (int l = plainPassword.length(); l > 0; --l) 
        {
            hiddenPassword.append("*");
        }
       return hiddenPassword.toString();
    }

    
    @Override
    protected void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
