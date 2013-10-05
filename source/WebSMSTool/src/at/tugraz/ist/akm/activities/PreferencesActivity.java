package at.tugraz.ist.akm.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.LogClient;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener
{
    LogClient mLog = new LogClient(this);
    private int mMinPortNumber = 1024, mMaxPortNumber = 65535;
    private String mDefaultServerProtocol = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_list);
        SharedPreferences preferences = sharedPreferences();
        mDefaultServerProtocol = resourceString(R.string.preference_server_protocol_default_value);

        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_username_key));
        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_password_key));
        updateCheckboxDependingOnCredentials();
        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_server_port_key));
        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_server_protocol_key));
    }


    private SharedPreferences sharedPreferences()
    {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
    }


    private String resourceString(int resourceStringId)
    {
        return getResources().getString(resourceStringId);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        mLog.debug("shared preferences key changed: [" + key + "]");
        if (key.equals(resourceString(R.string.preferences_server_port_key)))
        {
            int changedPortNumber = 0;
            try
            {
                changedPortNumber = Integer.parseInt(sharedPreferences
                        .getString(key, "8888"));
            } catch (NumberFormatException e)
            {
                changedPortNumber = 8888;
                mLog.warning("ignoring invalid user input");
            }

            changedPortNumber = trimPortNumber(changedPortNumber);
            Editor ed = sharedPreferences.edit();
            ed.putString(key, Integer.toString(changedPortNumber));
            ed.apply();
            ((EditTextPreference) findPreference(key)).setText(Integer.toString(changedPortNumber));
        } else if (key
                .equals(resourceString(R.string.preferences_server_protocol_key)))
        {
            String serverProtocol = sharedPreferences.getString(key,
                    mDefaultServerProtocol);

            serverProtocol = getProtocolFallback(serverProtocol);
            Editor ed = sharedPreferences.edit();
            ed.putString(key, serverProtocol);
            ed.apply();
        }
        setPreferenceSummary(sharedPreferences, key);
    }


    private String getProtocolFallback(String doubtfulProtocol)
    {
        String fallback = mDefaultServerProtocol;

        String[] protocols = getResources().getStringArray(
                R.array.preference_server_prococol_values);
        for (String p : protocols)
        {
            if (p.equals(doubtfulProtocol))
            {
                fallback = doubtfulProtocol;
                break;
            }
        }
        mLog.debug("protocol fallback from [" + doubtfulProtocol + "] to ["
                + fallback + "]");
        return fallback;
    }


    private int trimPortNumber(int uncheckedNumber)
    {
        int checkedPortNumber = uncheckedNumber;
        if (uncheckedNumber < mMinPortNumber)
        {
            checkedPortNumber = mMinPortNumber;
        }
        else if (uncheckedNumber > mMaxPortNumber)
        {
            checkedPortNumber = mMaxPortNumber;
        }
        
        mLog.debug("trimmed from [" + uncheckedNumber + "] to ["
                + checkedPortNumber + "]");
        return checkedPortNumber;
    }


    private void setPreferenceSummary(SharedPreferences sharedPref, String key)
    {
        try
        {
            Preference preferenceItem = findPreference(key);
            if (null != preferenceItem)
            {
                String summary = new String(sharedPref.getString(key, ""));
                if (key.equals(resourceString(R.string.preferences_password_key)))
                {
                    summary = getCoveredPassword(summary);
                }
                preferenceItem.setSummary(summary);
            }
        } catch (ClassCastException c)
        {
            mLog.debug("ignored preference summary: " + c.getMessage());
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


    @Override
    protected void onStop()
    {
        updateCheckboxDependingOnCredentials();
        super.onStop();
    }


    private void updateCheckboxDependingOnCredentials()
    {
        SharedPreferences sharedPrefs = sharedPreferences();
        String password = sharedPrefs.getString(resourceString(R.string.preferences_password_key), "");
        String username = sharedPrefs.getString(resourceString(R.string.preferences_username_key), "");
        CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(resourceString(R.string.prefrences_access_restriction_key));
        
        if ( username.length() <= 0 || password.length() <= 0) {
            Editor spEdit = sharedPrefs.edit();
            spEdit.putBoolean(resourceString(R.string.prefrences_access_restriction_key), true);
            spEdit.apply();
            checkBox.setChecked(false);
        } else
        {
            checkBox.setChecked(true);
        }
    }
}
