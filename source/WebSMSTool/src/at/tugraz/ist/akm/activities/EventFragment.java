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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.ui.IUiLogSink;
import at.tugraz.ist.akm.trace.ui.UiEvent;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceMessageBuilder;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.VerboseMessageSubmitter;

public class EventFragment extends Fragment implements IUiLogSink,
        ServiceConnection
{
    private static final String EVENT_MESSAGE_ICON_KEY = "EVENT_MESSAGE_ICON_KEY";
    private static final String EVENT_MESSAGE_DATE_KEY = "EVENT_MESSAGE_DATE_KEY";
    private static final String EVENT_MESSAGE_TIME_KEY = "EVENT_MESSAGE_TIME_KEY";
    private static final String EVENT_MESSAGE_TITLE_KEY = "EVENT_MESSAGE_TITLE_KEY";
    private static final String EVENT_MESSAGE_DESCRIPTION_KEY = "EVENT_MESSAGE_DESCRIPTION_KEY";
    private static final String EVENT_MESSAGE_DETAIL_KEY = "EVENT_MESSAGE_DETAIL_KEY";

    private LogClient mLog = new LogClient(
            EventFragment.class.getCanonicalName());
    private ListView mListView = null;
    private LinkedList<HashMap<String, String>> mEventLogListData = new LinkedList<HashMap<String, String>>();
    private SimpleAdapter mListViewAdapter = null;
    private boolean mIsUnbindingFromService = false;

    private String mClientName = "EventClient";
    private String mServiceName = "service";
    private Messenger mServiceMessenger = null;
    private VerboseMessageSubmitter mMessageSender = new VerboseMessageSubmitter(
            null, mClientName, mServiceName);
    private EventClientIncomingServiceMessageHandler mIncomingServiceMessageHandler = new EventClientIncomingServiceMessageHandler(
            this);
    private Messenger mClientMessenger = new Messenger(
            mIncomingServiceMessageHandler);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        mLog.debug("on create view");
        View view = inflater.inflate(R.layout.event_list_fragment, container,
                false);
        mListView = (ListView) view.findViewById(R.id.event_list_list);
        mListViewAdapter = newListAdapter();
        mListView.setAdapter(mListViewAdapter);
        return view;
    }


    private SimpleAdapter newListAdapter()
    {
        String[] fromMapping = { EVENT_MESSAGE_ICON_KEY,
                EVENT_MESSAGE_DATE_KEY, EVENT_MESSAGE_TIME_KEY,
                EVENT_MESSAGE_TITLE_KEY, EVENT_MESSAGE_DESCRIPTION_KEY,
                EVENT_MESSAGE_DETAIL_KEY };
        int[] toMapping = { R.id.event_list_icon, R.id.event_list_date,
                R.id.event_list_time, R.id.event_list_title,
                R.id.event_list_description, R.id.event_list_detail };

        return new SimpleAdapter(getActivity().getBaseContext(),
                mEventLogListData, R.layout.event_list_entry, fromMapping,
                toMapping);
    }


    private void tearDownUi()
    {
        mListView = null;
    }


    @Override
    public void onStart()
    {
        mLog.debug("on start");
        super.onStart();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLog.debug("on create");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mLog.debug("on configuration changed");
    }


    @Override
    public void onDestroy()
    {
        mLog.debug("on destroy");
        super.onDestroy();
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mLog.debug("on destroy view");
    }


    @Override
    public void onPause()
    {
        mLog.debug("on pause");
        unbindFromService();
        tearDownUi();
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mLog.debug("on resume");
        bindToService();
    }


    @Override
    public void onDetach()
    {
        mLog.debug("on detach");
        super.onDetach();
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mLog.debug("on attach");
    }


    @Override
    public void info(UiEvent event)
    {
        mEventLogListData.addFirst(newLogFields(event));
        notifyListAdapterDataSetChanged();
    }


    @Override
    public void info(List<UiEvent> eventList)
    {
        ListIterator<UiEvent> pointer = eventList
                .listIterator(eventList.size());

        while (pointer.hasPrevious())
        {
            mEventLogListData.addFirst(newLogFields(pointer.previous()));
        }
        notifyListAdapterDataSetChanged();
    }


    private void notifyListAdapterDataSetChanged()
    {
        mListViewAdapter.notifyDataSetChanged();
    }


    private HashMap<String, String> newLogFields(UiEvent event)
    {
        HashMap<String, String> logFields = new HashMap<String, String>();
        logFields.put(EVENT_MESSAGE_ICON_KEY,
                Integer.toString(event.getDrawableIconId()));
        logFields.put(EVENT_MESSAGE_DATE_KEY, event.getDate());
        logFields.put(EVENT_MESSAGE_TIME_KEY, event.getTime());
        logFields.put(EVENT_MESSAGE_TITLE_KEY, event.getTitle());
        logFields.put(EVENT_MESSAGE_DESCRIPTION_KEY, event.getDescription());
        logFields.put(EVENT_MESSAGE_DETAIL_KEY, event.getDetail());
        return logFields;
    }


    protected void onWebServiceEvent(UiEvent event)
    {
        info(event);
    }


    protected void onWebServiceEvent(List<UiEvent> eventList)
    {
        info(eventList);
    }


    protected void onWebServiceClientRegistered()
    {
        mLog.debug("log client registered to service");
    }


    private void unbindFromService()
    {
        boolean hasServiceMessenger = mServiceMessenger != null;
        if (mIsUnbindingFromService == false && hasServiceMessenger)
        {
            askWebServiceForClientUnregistrationAsync();
            getActivity().getApplicationContext().unbindService(this);
            mServiceMessenger = null;
        } else
        {
            mLog.debug("skipped to UNbinding because isUNbinding ["
                    + mIsUnbindingFromService + "] and hasServiceMessenger ["
                    + hasServiceMessenger + "]");
        }
    }


    private void bindToService()
    {
        boolean hasServiceMessenger = mServiceMessenger != null;
        if (mIsUnbindingFromService == false && hasServiceMessenger == false)
        {
            getActivity().getApplicationContext().bindService(
                    newServiceIntent(), this, Context.BIND_AUTO_CREATE);
        } else
        {
            mLog.error("failed to bind because isUNbinding ["
                    + mIsUnbindingFromService + "] and hasServiceMessenger ["
                    + hasServiceMessenger + "]");
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        String serviceComponentNameSuffix = WebSMSToolService.class
                .getSimpleName();
        String inServiceName = name.flattenToShortString();
        if (inServiceName.endsWith(serviceComponentNameSuffix))
        {
            mLog.debug("bound to service [" + serviceComponentNameSuffix + "]");
            mServiceMessenger = new Messenger(service);
            mMessageSender = new VerboseMessageSubmitter(mServiceMessenger,
                    mClientName, mServiceName);
            askWebServiceForClientRegistrationAsync();
        } else
        {
            mLog.error("failed binding fragment to service[" + inServiceName
                    + "] expected [*" + serviceComponentNameSuffix + "]");
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        String inServiceName = name.flattenToShortString();
        String serviceComponentNameSuffix = WebSMSToolService.class
                .getSimpleName();
        if (inServiceName.endsWith(serviceComponentNameSuffix))
        {
            mLog.debug("unbound from service [" + serviceComponentNameSuffix
                    + "]");
            mServiceMessenger = null;
            mIsUnbindingFromService = false;
        } else
        {
            mLog.error("failed unbinding fragment to service[" + inServiceName
                    + "] expected [*" + serviceComponentNameSuffix + "]");
        }

    }


    private void askWebServiceForClientUnregistrationAsync()
    {
        mMessageSender.submit(ServiceMessageBuilder
                .newEventClientUnregistrationMessage());
    }


    private void askWebServiceForClientRegistrationAsync()
    {
        mMessageSender.submit(ServiceMessageBuilder
                .newEventClientRegistrationMessage(mClientMessenger));
    }


    private Intent newServiceIntent()
    {
        return new Intent(getActivity(), WebSMSToolService.class);
    }

}
