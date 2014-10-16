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

package at.tugraz.ist.akm.sms;

import java.io.Serializable;

public class TextMessage implements Serializable
{

    private static final long serialVersionUID = 6681600779409479945L;

    private String mId = "";
    private String mThreadId = "";
    private String mPerson = "";
    private String mDate = "";
    /**
     * usually the phone number
     */
    private String mAddress = "";
    private String mSeen = "1";
    private String mRead = "1";
    private String mBody = "";
    private String mProtocol = "null";
    private String mStatus = "-1";
    private String mType = "";
    private String mServiceCenter = "null";
    private String mLocked = "0";
    private String mReplyPathPresent = "";
    private String mSubject = "";
    private String mErrorCode = "";


    public TextMessage()
    {
    }


    public TextMessage(final TextMessage m)
    {
        mId = m.mId;
        mThreadId = m.mThreadId;
        mPerson = m.mPerson;
        mDate = m.mDate;
        mAddress = m.mAddress;
        mSeen = m.mSeen;
        mRead = m.mRead;
        mBody = m.mBody;
        mProtocol = m.mProtocol;
        mStatus = m.mStatus;
        mType = m.mType;
        mServiceCenter = m.mServiceCenter;
        mLocked = m.mLocked;
        mReplyPathPresent = m.mReplyPathPresent;
        mSubject = m.mSubject;
        mErrorCode = m.mErrorCode;
    }


    public String getReplyPathPresent()
    {
        return mReplyPathPresent;
    }


    public void setReplyPathPresent(String replyPathPresent)
    {
        this.mReplyPathPresent = replyPathPresent;
    }


    public String getSubject()
    {
        return mSubject;
    }


    public TextMessage setSubject(String subject)
    {
        this.mSubject = subject;
        return this;
    }


    public String getErrorCode()
    {
        return mErrorCode;
    }


    public TextMessage setErrorCode(String errorCode)
    {
        this.mErrorCode = errorCode;
        return this;
    }


    public String getId()
    {
        return mId;
    }


    public TextMessage setId(String id)
    {
        mId = id;
        return this;
    }


    public String getThreadId()
    {
        return mThreadId;
    }


    public TextMessage setThreadId(String threadId)
    {
        mThreadId = threadId;
        return this;
    }


    public String getPerson()
    {
        return mPerson;
    }


    public TextMessage setPerson(String person)
    {
        mPerson = person;
        return this;
    }


    public String getDate()
    {
        return mDate;
    }


    public TextMessage setDate(String date)
    {
        this.mDate = date;
        return this;
    }


    public String getAddress()
    {
        return mAddress;
    }


    public TextMessage setAddress(String address)
    {
        mAddress = address;
        return this;
    }


    public String getSeen()
    {
        return mSeen;
    }


    public TextMessage setSeen(String seen)
    {
        mSeen = seen;
        return this;
    }


    public String getRead()
    {
        return mRead;
    }


    public TextMessage setRead(String read)
    {
        mRead = read;
        return this;
    }


    public String getBody()
    {
        return mBody;
    }


    public TextMessage setBody(String body)
    {
        mBody = body;
        return this;
    }


    public String getProtocol()
    {
        return mProtocol;
    }


    public TextMessage setProtocol(String protocol)
    {
        mProtocol = protocol;
        return this;
    }


    public String getStatus()
    {
        return mStatus;
    }


    public TextMessage setStatus(String status)
    {
        mStatus = status;
        return this;
    }


    public String getType()
    {
        return mType;
    }


    public TextMessage setType(String type)
    {
        mType = type;
        return this;
    }


    public String getServiceCenter()
    {
        return mServiceCenter;
    }


    public TextMessage setServiceCenter(String serviceCenter)
    {
        mServiceCenter = serviceCenter;
        return this;
    }


    public String getLocked()
    {
        return mLocked;
    }


    public TextMessage setLocked(String locked)
    {
        mLocked = locked;
        return this;
    }

}
