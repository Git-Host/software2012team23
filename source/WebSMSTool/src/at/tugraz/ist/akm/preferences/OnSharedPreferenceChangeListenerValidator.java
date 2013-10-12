package at.tugraz.ist.akm.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.LogClient;

public class OnSharedPreferenceChangeListenerValidator implements
        OnSharedPreferenceChangeListener
{
    private int mMinPortNumber = 1024, mMaxPortNumber = 65535;
    private LogClient mLog = new LogClient(this);
    private String mDefaultServerProtocol = null;
    private Resources mResource = null;


    public OnSharedPreferenceChangeListenerValidator(Resources resources)
    {
        mResource = resources;
        mDefaultServerProtocol = resourceString(R.string.preference_server_protocol_default_value);

    }


    @Override
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
    }


    private String getProtocolFallback(String doubtfulProtocol)
    {
        String fallback = mDefaultServerProtocol;

        String[] protocols = mResource
                .getStringArray(R.array.preference_server_prococol_values);
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
        return mResource.getString(resourceStringId);
    }
}
