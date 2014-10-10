package at.tugraz.ist.akm.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.LogClient;

public class OnSharedPreferenceEventListenValidator implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener
{

    private int mMinPortNumber = 1024;
    private int mMaxPortNumber = 65535;
    private LogClient mLog = new LogClient(this);
    private PreferenceFragment mFragment = null;
    private Context mContext = null;


    public OnSharedPreferenceEventListenValidator(PreferenceFragment fragment,
            Context context)
    {
        mFragment = fragment;
        mContext = context;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        mLog.debug("shared preferences key changed: [" + key + "]");
        if (key.equals(resourceString(R.string.preferences_server_port_key)))
        {
            String defaultPortString = resourceString(R.string.OnSharedPreferenceEventListenValidator_default_port);
            int changedPortNumber = 0;
            try
            {
                changedPortNumber = Integer.parseInt(sharedPreferences
                        .getString(key, defaultPortString));
            }
            catch (NumberFormatException e)
            {
                try
                {
                    changedPortNumber = Integer.parseInt(defaultPortString);
                }
                catch (NumberFormatException f)
                {
                    changedPortNumber = 8888;
                }
                mLog.warning("ignoring invalid user input");
            }

            changedPortNumber = trimPortNumber(changedPortNumber);
            Editor ed = sharedPreferences.edit();
            ed.putString(key, Integer.toString(changedPortNumber));
            ed.commit();
        }

        updateSettingsOnPrefsView();
    }


    private int trimPortNumber(int uncheckedNumber)
    {
        int checkedPortNumber = uncheckedNumber;
        if (uncheckedNumber < mMinPortNumber)
        {
            checkedPortNumber = mMinPortNumber;
        } else if (uncheckedNumber > mMaxPortNumber)
        {
            checkedPortNumber = mMaxPortNumber;
        }

        mLog.debug("trimmed from [" + uncheckedNumber + "] to ["
                + checkedPortNumber + "]");
        return checkedPortNumber;
    }


    private String resourceString(int resourceStringId)
    {
        return mContext.getResources().getString(resourceStringId);
    }


    private void setPreferenceSummary(String key)
    {
        SharedPreferences sharedPref = sharedPreferences();
        try
        {
            Preference preferenceItem = mFragment.findPreference(key);
            if (null != preferenceItem)
            {
                String summary = "";
                try
                {
                    summary = sharedPref.getString(key, "");
                }
                catch (Exception e)
                {
                    mLog.error("shared preference key not found", e);
                }

                if (key.equals(resourceString(R.string.preferences_password_key)))
                {
                    summary = getCoveredPassword(summary);
                }
                try
                {
                    preferenceItem.setSummary(summary);
                }
                catch (Exception e)
                {
                    mLog.error("shared preference set summary error", e);
                }
            }
        }
        catch (ClassCastException c)
        {
            mLog.debug("ignored preference summary: " + c.getMessage());
        }
    }


    private SharedPreferences sharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }


    private void updateAccessRestrictionCheckboxDependingOnCredentials()
    {
        SharedPreferences sharedPrefs = sharedPreferences();
        String password = sharedPrefs.getString(
                resourceString(R.string.preferences_password_key), "");
        String username = sharedPrefs.getString(
                resourceString(R.string.preferences_username_key), "");
        CheckBoxPreference checkBox = (CheckBoxPreference) mFragment
                .findPreference(resourceString(R.string.preferences_access_restriction_key));

        Editor spEdit = sharedPrefs.edit();
        if (username.length() <= 0 || password.length() <= 0)
        {

            spEdit.putBoolean(
                    resourceString(R.string.preferences_access_restriction_key),
                    false);
            checkBox.setChecked(false);
        }
        spEdit.commit();
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


    public synchronized void updateSettingsOnPrefsView()
    {
        setPreferenceSummary(resourceString(R.string.preferences_username_key));
        setPreferenceSummary(resourceString(R.string.preferences_password_key));
        setPreferenceSummary(resourceString(R.string.preferences_server_port_key));
    }


    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        return handlePreference(preference);
    }


    private boolean handlePreference(Preference preference)
    {
        mLog.debug("preference clicked [" + preference + "]");
        String preferenceKey = preference.getKey();

        if (preferenceKey
                .equals(resourceString(R.string.preferences_access_restriction_key)))
        {
            mLog.debug("clicked access checkbox");
        } else if (preferenceKey
                .equals(resourceString(R.string.preferences_password_key)))
        {
            mLog.debug("clicked password");
        } else if (preferenceKey
                .equals(resourceString(R.string.preferences_server_port_key)))
        {
            mLog.debug("clicked server port");
            updateAccessRestrictionCheckboxDependingOnCredentials();
        } else if (preferenceKey
                .equals(resourceString(R.string.preferences_protocol_checkbox_key)))
        {
            mLog.debug("clicked server protocol");
            updateAccessRestrictionCheckboxDependingOnCredentials();
        } else if (preferenceKey
                .equals(resourceString(R.string.preferences_username_key)))
        {
            mLog.debug("clicked username");
        } else
        {
            mLog.debug(" -->> clicked " + preferenceKey);
        }

        return false;
    }

}
