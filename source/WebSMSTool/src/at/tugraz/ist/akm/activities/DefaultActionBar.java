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
        Intent i = null;
        switch (item.getItemId())
        {
        // case android.R.id.home:
        // NavUtils.navigateUpFromSameTask(this);
        // //i = new Intent(this, MainActivity.class);
        // //startActivity(i);
        // return true;
        case android.R.id.home:
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent))
            {
                // This activity is NOT part of this app's task, so create a new
                // task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else
            {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        case R.id.actionbar_about:
            i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        case R.id.actionbar_settings:
            i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
            return true;
        default:
            TraceService.log(
                    LogLevel.INFO,
                    "FRANZ",
                    new StringBuffer("actionbar intent ").append(
                            Integer.toHexString(item.getItemId())).toString(),
                    null);
            i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
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
