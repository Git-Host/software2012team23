package at.tugraz.ist.akm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Debug;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.preferences.PreferencesActivity;
import at.tugraz.ist.akm.exceptional.UncaughtExceptionLogger;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.trace.TraceService.LogLevel;

public class DefaultActionBar extends Activity
{

    LogClient mLog = new LogClient(DefaultActionBar.class.getCanonicalName());


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        mLog.debug("handling [" + item + "]");
        Intent i = null;
        switch (item.getItemId())
        {
        case android.R.id.home:
            Intent upIntent = NavUtils.getParentActivityIntent(this);

            if (upIntent == null)
            {
                upIntent = new Intent(getApplicationContext(),
                        MainActivity.class);
            }

            mLog.debug("intent [" + upIntent + "]");
            if (NavUtils.shouldUpRecreateTask(this, upIntent))
            {
                TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities();
            } else
            {
                NavUtils.navigateUpTo(this, upIntent);
            }
            return false;
        case R.id.actionbar_about:
            i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        case R.id.actionbar_settings:
            i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
            return true;
        default:
            mLog.debug(
                    new StringBuffer("unhandled actionbar intent [").append(
                            Integer.toHexString(item.getItemId()) + "]")
                            .toString(), null);
            i = new Intent(this, MainActivity.class);
            startActivity(i);
            return false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (Debug.isDebuggerConnected())
        {
            UncaughtExceptionLogger exLogger = new UncaughtExceptionLogger(mLog);
            exLogger.register();
        }

        MenuInflater inflater = getMenuInflater();
        getActionBar().setIcon(
                getResources().getDrawable(R.drawable.ic_notification));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        inflater.inflate(R.menu.default_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
