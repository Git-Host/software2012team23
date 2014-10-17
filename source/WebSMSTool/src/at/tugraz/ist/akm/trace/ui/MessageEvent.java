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

package at.tugraz.ist.akm.trace.ui;

import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.sms.TextMessage;

public class MessageEvent extends UiEvent
{
    private boolean mIsIncomingMessage = true;


    @SuppressWarnings("unused")
    private MessageEvent()
    {
    }


    public MessageEvent(boolean isIncomingMessage)
    {
        mIsIncomingMessage = isIncomingMessage;
    }


    public UiEvent load(ResourceStringLoader loader, TextMessage message)
    {
        if (mIsIncomingMessage)
        {
            setDrawableIconId(R.drawable.ic_action_messages);
            setTitle(loader.getReceivedTitle());

        } else
        {
            setDrawableIconId(R.drawable.ic_action_content_read);
            setTitle(loader.getSentTitle());
        }
        setDescription(message.getAddress());
        setDetail(message.getBody());
        return super.load(loader);
    }
}
