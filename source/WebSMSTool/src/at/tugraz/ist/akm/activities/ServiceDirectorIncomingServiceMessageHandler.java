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

import android.os.Handler;
import android.os.Message;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceConnectionMessageTypes;

public class ServiceDirectorIncomingServiceMessageHandler extends Handler
{
    LogClient mLog = new LogClient(
            ServiceDirectorIncomingServiceMessageHandler.class.getCanonicalName());
    StartServiceFragment mClientFragment = null;


    public ServiceDirectorIncomingServiceMessageHandler(StartServiceFragment client)
    {
        mClientFragment = client;
    }


    public void onClose()
    {
        mClientFragment = null;
    }


    @Override
    public void handleMessage(Message msg)
    {
        try
        {
            mLog.debug("incoming service message ["
                    + ServiceConnectionMessageTypes.getMessageName(msg.what)
                    + "]");
            switch (msg.what)
            {

            case ServiceConnectionMessageTypes.Service.Response.SERVICE_EVENT:

//                UiEvent event = msg
//                        .getData()
//                        .getParcelable(
//                                ServiceConnectionMessageTypes.Bundle.Key.PARCELABLE_UI_EVENT);
//                mClientFragment.onWebServiceEvent(event);
                break;

            case ServiceConnectionMessageTypes.Service.Response.SMS_DELIVERED:
                mClientFragment.onWebServiceSmsDelivered(msg.arg1);
                break;
            case ServiceConnectionMessageTypes.Service.Response.SMS_RECEIVED:
                mClientFragment.onWebServiceSmsReceived(msg.arg1);
                break;
            case ServiceConnectionMessageTypes.Service.Response.SMS_SENT:
                mClientFragment.onWebServiceSmsSent(msg.arg1);
                break;
            case ServiceConnectionMessageTypes.Service.Response.SMS_SENT_ERRONEOUS:
                mClientFragment.onWebServiceSmsSentErroneous(msg.arg1);
                break;

            case ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_RX_BYTES:
                mClientFragment.onWebServiceRxBytesUpdate(msg.arg1);
                break;
            case ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_TX_BYTES:
                mClientFragment.onWebServiceTxBytesUpdate(msg.arg1);
                break;

            case ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL:
                mClientFragment
                        .onWebServiceURLChanged(msg
                                .getData()
                                .getString(
                                        ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_CONNECTION_URL));
                break;

            case ServiceConnectionMessageTypes.Service.Response.REGISTERED_TO_SERVICE_MANAGEMENT:
                mClientFragment.onWebServiceClientRegistered();
                break;

            case ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD:
                mClientFragment
                        .onWebServiceHttpPassword(msg
                                .getData()
                                .getString(
                                        ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD));
                break;

            case ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME:
                mClientFragment
                        .onWebServiceHttpUsername(msg
                                .getData()
                                .getString(
                                        ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME));
                break;

            case ServiceConnectionMessageTypes.Service.Response.HTTP_ACCESS_RESCRICTION_ENABLED:
                mClientFragment.onWebServiceHttpAccessRestriction(msg.arg1);
                break;

            case ServiceConnectionMessageTypes.Service.Response.NETWORK_NOT_AVAILABLE:
                mClientFragment.onWebServiceNetworkNotConnected();
                break;

            case ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE:
                switch (msg.arg1)
                {
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_BEFORE_SINGULARITY:
                    mClientFragment.onWebServiceRunningStateBeforeSingularity();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_RUNNING:
                    mClientFragment.onWebServiceRunning();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTED_ERRONEOUS:
                    mClientFragment.onWebServiceStartErroneous();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STARTING:
                    mClientFragment.onWebServiceStarting();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED:
                    mClientFragment.onWebServiceStopped();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPED_ERRONEOUS:
                    mClientFragment.onWebServiceStoppedErroneous();
                    break;
                case ServiceConnectionMessageTypes.Service.Response.RUNNING_STATE_STOPPING:
                    mClientFragment.onWebServiceStopping();
                    break;
                default:
                    mLog.debug("failed handling ["
                            + ServiceConnectionMessageTypes
                                    .getMessageName(ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE)
                            + "] = ["
                            + ServiceConnectionMessageTypes
                                    .getMessageName(msg.arg1) + "]");
                    break;
                }
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
        catch (NullPointerException e)
        {
            mLog.error("failed reading message from client");
            mLog.debug("failed reading message from client", e);
        }
    }
}
