/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.trace.AndroidUILogSink;
import at.tugraz.ist.akm.exceptional.UncaughtExceptionLogger;
import at.tugraz.ist.akm.secureRandom.PRNGFixes;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.webservice.WebSMSToolService;

public class MainActivity extends Activity
{

    public static final String SERVER_IP_ADDRESS_INTENT_KEY = "at.tugraz.ist.akm.SERVER_IP_ADDRESS_INTENT_KEY";
    private static final String LAST_ACTIVE_NAVIGATION_DRAWER_ENTRY_KEY = "at.tugraz.ist.akm.LAST_ACTIVE_NAVIGATION_DRAWER_ITEM_KEY";

    private int mCurrentNavigationDrawerEntry = 0;

    private LogClient mLog = new LogClient(this);
    final String mServiceName = WebSMSToolService.class.getName();

    private String[] mDrawerEntryTitles = null;
    private String[] mDrawerIcons = null;
    private String[] mDrawerFragments = null;
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;

    private String mDefaultAppPackage = "at.tugraz.ist.akm";
    private String mDefaultSystemPackage = "android";


    public MainActivity()
    {
        mLog.debug("constructing " + getClass().getSimpleName());
        PRNGFixes.apply();
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mLog.debug("brought activity to front");
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mLog.debug("user returned to activity");
    }


    @Override
    protected void onPause()
    {
        mLog.debug("activity goes to background");
        super.onStop();
    }


    @Override
    protected void onStop()
    {
        mLog.debug("activity no longer visible");
        super.onStop();
    }


    @Override
    protected void onDestroy()
    {
        mLog.debug("activity goes to Hades");
        mLog = null;
        super.onDestroy();
    }


    private SimpleAdapter newItemDrawerAdapter()
    {
        mDrawerEntryTitles = getResources().getStringArray(
                R.array.drawer_string_array);
        mDrawerIcons = getResources().getStringArray(R.array.drawer_icon_array);

        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < mDrawerFragments.length; i++)
        {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("icon",
                    Integer.toString(getDrawableIdentifier(mDrawerIcons[i])));
            map.put("title", mDrawerEntryTitles[i]);
            data.add(map);
        }

        String[] fromMapping = { "icon", "title" };
        int[] toMapping = { R.id.drawer_item_icon, R.id.drawer_item_text };

        return new SimpleAdapter(getBaseContext(), data,
                R.layout.navigation_drawer_list_entry, fromMapping, toMapping);
    }


    private int getDrawableIdentifier(String drawable)
    {
        int id = getResources().getIdentifier(drawable, "drawable",
                mDefaultAppPackage);

        if (id == 0)
        {
            id = getResources().getIdentifier(drawable, "drawable",
                    mDefaultSystemPackage);
        }
        return id;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState)
        {
            mCurrentNavigationDrawerEntry = savedInstanceState
                    .getInt(LAST_ACTIVE_NAVIGATION_DRAWER_ENTRY_KEY);
        }

        setContentView(R.layout.navigation_drawer);
        mDrawerFragments = getResources().getStringArray(
                R.array.drawer_fragment_array);
        mDrawerEntryTitles = getResources().getStringArray(
                R.array.drawer_string_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer_left_drawer);

        mDrawerList.setAdapter(newItemDrawerAdapter());

        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    final int pos, long id)
            {
                mCurrentNavigationDrawerEntry = pos;
                mDrawerLayout
                        .setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                            @Override
                            public void onDrawerClosed(View drawerView)
                            {
                                super.onDrawerClosed(drawerView);

                                MainActivity.this
                                        .fragmentTransaction(mDrawerFragments[pos]);
                            }
                        });
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        fragmentTransaction(mDrawerFragments[mCurrentNavigationDrawerEntry]);
        mDrawerList.setItemChecked(mCurrentNavigationDrawerEntry, true);
        setUpDrawerToggle();
        // TODO: replace sink with a kind of buffered logging sink
        TraceService.setSink(new AndroidUILogSink(this));
        mLog.debug("launched activity on device [" + Build.PRODUCT + "]");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(LAST_ACTIVE_NAVIGATION_DRAWER_ENTRY_KEY,
                mCurrentNavigationDrawerEntry);
        super.onSaveInstanceState(outState);
    }


    private void fragmentTransaction(String fragmentTag)
    {
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();

        Fragment newFragment = Fragment.instantiate(MainActivity.this,
                fragmentTag);

        transaction.replace(R.id.navigation_drawer_content_frame, newFragment,
                fragmentTag);

        transaction.commit();
    }


    @Override
    public void onBackPressed()
    {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (null == getFragmentManager().findFragmentByTag(
                mDrawerFragments[0]))
        {
            fragmentTransaction(mDrawerFragments[0]);
        } else
        {
            finish();
        }
    }


    private void setUpDrawerToggle()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                invalidateOptionsMenu();
            }


            @Override
            public void onDrawerOpened(View drawerView)
            {
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run()
            {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
        inflater.inflate(R.menu.default_actionbar, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

}
