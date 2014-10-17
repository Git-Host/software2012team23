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

package at.tugraz.ist.akm.webservice.service.interProcessMessges;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import at.tugraz.ist.akm.trace.LogClient;

public class VerboseMessageSubmitter
{

    private LogClient mLog = new LogClient(
            VerboseMessageSubmitter.class.getCanonicalName());
    private String mFrom = null;
    private String mTo = null;
    private Messenger mToMessenger = null;


    @SuppressWarnings("unused")
    private VerboseMessageSubmitter()
    {
    }


    public VerboseMessageSubmitter(Messenger receiver,
            String senderHumanReadableName, String receiverHumanReadableName)
    {
        mFrom = senderHumanReadableName;
        mTo = receiverHumanReadableName;
        mToMessenger = receiver;
    }


    private String toHumanReadable(Message message)
    {
        StringBuilder humanReadableMessage = new StringBuilder();
        String what = "what="
                + ServiceConnectionMessageTypes.getMessageName(message.what);
        String arg1 = " arg1="
                + ((message.arg1 == 0) ? "0" : ServiceConnectionMessageTypes
                        .getMessageName(message.arg1));
        String arg2 = " arg2="
                + ((message.arg2 == 0) ? "0" : ServiceConnectionMessageTypes
                        .getMessageName(message.arg2));
        String hasObject = " has_object="
                + ((message.obj == null) ? "no" : "yes");
        String hasBundle = " has_bundle="
                + ((message.getData().isEmpty() == true) ? "no" : "yes");

        humanReadableMessage.append("msg:{").append(what).append(arg1)
                .append(arg2).append(hasObject).append(hasBundle);

        humanReadableMessage.append(" key_set=[");
        boolean isFirstKey = true;
        if (message.getData().isEmpty() == false)
        {
            for (String key : message.getData().keySet())
            {
                if (isFirstKey)
                {
                    humanReadableMessage.append(key);
                    isFirstKey = false;
                }
                humanReadableMessage.append(" " + key);
            }
        }
        humanReadableMessage.append("]}");
        return humanReadableMessage.toString();
    }


    public void submit(Message message)
    {
        String prettyMessage = toHumanReadable(message);
        String simpleMessageName = ServiceConnectionMessageTypes
                .getMessageName(message.what);

        mLog.debug(mFrom + " -> " + mTo + " " + prettyMessage);
        if (mToMessenger != null)
        {
            try
            {
                mToMessenger.send(message);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending [" + simpleMessageName + "] to "
                        + mTo, e);
            }
        } else
        {
            mLog.error("failed sending [" + simpleMessageName + "] to " + mTo
                    + " == null");
        }
    }
}
