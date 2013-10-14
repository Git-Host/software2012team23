package at.tugraz.ist.akm.activities;

import java.security.cert.X509Certificate;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.keystore.ApplicationKeyStore;
import at.tugraz.ist.akm.preferences.OnSharedPreferenceChangeListenerValidator;
import at.tugraz.ist.akm.preferences.PreferencesProvider;
import at.tugraz.ist.akm.trace.LogClient;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener
{
    private LogClient mLog = new LogClient(this);
    private OnSharedPreferenceChangeListenerValidator mPreferenceListener = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_list);
        SharedPreferences preferences = sharedPreferences();

        mPreferenceListener = new OnSharedPreferenceChangeListenerValidator(getApplicationContext());

        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_username_key));
        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_password_key));
        updateCheckboxDependingOnCredentials();
        setPreferenceSummary(preferences,
                resourceString(R.string.preferences_server_port_key));
        setPreferenceSummary(
                preferences,
                resourceString(R.string.preferences_protocol_renew_certificate_checkbox_key));

    }


    private SharedPreferences sharedPreferences()
    {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(mPreferenceListener);
        sp.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        sp.registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }


    @Override
    protected void onStop()
    {
        updateCheckboxDependingOnCredentials();
        super.onStop();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        mLog.debug("shared preferences key changed: [" + key + "]");
        setPreferenceSummary(sharedPreferences, key);
    }


    private void updateCheckboxDependingOnCredentials()
    {
        SharedPreferences sharedPrefs = sharedPreferences();
        String password = sharedPrefs.getString(
                resourceString(R.string.preferences_password_key), "");
        String username = sharedPrefs.getString(
                resourceString(R.string.preferences_username_key), "");
        CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(resourceString(R.string.prefrences_access_restriction_key));

        if (username.length() <= 0 || password.length() <= 0)
        {
            Editor spEdit = sharedPrefs.edit();
            spEdit.putBoolean(
                    resourceString(R.string.prefrences_access_restriction_key),
                    true);
            spEdit.apply();
            checkBox.setChecked(false);
        } else
        {
            checkBox.setChecked(true);
        }
    }


    private void setPreferenceSummary(SharedPreferences sharedPref, String key)
    {
        try
        {
            Preference preferenceItem = findPreference(key);
            if (null != preferenceItem)
            {
                String summary = "";
                try
                {
                    summary = sharedPref.getString(key, "");
                }
                catch (Exception e)
                {
                    // i don't care
                }

                if (key.equals(resourceString(R.string.preferences_password_key)))
                {
                    summary = getCoveredPassword(summary);
                } else if (key
                        .equals(resourceString(R.string.preferences_protocol_renew_certificate_checkbox_key)))
                {
                    summary = createRenewCertificateCheckboxSummary();
                    Editor spEdit = sharedPref.edit();
                    spEdit.putBoolean(
                            resourceString(R.string.preferences_protocol_renew_certificate_checkbox_key),
                            false);
                    spEdit.apply();

                }
                try
                {
                    preferenceItem.setSummary(summary);
                }
                catch (Exception e)
                {
                    // i don't care
                }
            }
        }
        catch (ClassCastException c)
        {
            mLog.debug("ignored preference summary: " + c.getMessage());
        }
    }


    private String createRenewCertificateCheckboxSummary()
    {
        String summary = null;
        PreferencesProvider preferencesProvider = new PreferencesProvider(
                getApplicationContext());
        ApplicationKeyStore appKeyStore = new ApplicationKeyStore();

        appKeyStore.loadKeystore(preferencesProvider.getKeyStorePassword(),
                preferencesProvider.getKeyStoreFilePath());
        X509Certificate cert = appKeyStore.getX509Certficate();
        appKeyStore.close();

        if (cert != null)
        {
            summary = "SN " + cert.getSerialNumber().toString() + " Expires "
                    + cert.getNotAfter();
            appKeyStore.close();
        } else
        {
            summary = "no certificate available";
        }
        return summary;

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


    private String resourceString(int resourceStringId)
    {
        return getResources().getString(resourceStringId);
    }

}
