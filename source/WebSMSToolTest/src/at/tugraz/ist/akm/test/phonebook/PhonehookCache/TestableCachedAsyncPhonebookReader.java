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

package at.tugraz.ist.akm.test.phonebook.PhonehookCache;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.PhonebookCache.CacheStates;
import at.tugraz.ist.akm.phonebook.PhonebookCache.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;
import at.tugraz.ist.akm.trace.LogClient;

public class TestableCachedAsyncPhonebookReader extends
        CachedAsyncPhonebookReader
{
    private int mIsTrheadWaiting = -1;
    private LogClient mLog = new LogClient(
            TestableCachedAsyncPhonebookReader.class.getCanonicalName());

    private CacheStates mBreakPoint = CacheStates.READY_FOR_CHANGES;
    private boolean mIsBreakpointEnabled = false;


    TestableCachedAsyncPhonebookReader(ContactFilter filter, Context context,
            IContactReader contactReader)
    {
        super(filter, context, contactReader);
    }


    public boolean isThreadWaiting()
    {
        return (mIsTrheadWaiting == 1);
    }


    public void setNextBreakpoint(CacheStates breakPoint)
    {
        mBreakPoint = breakPoint;
        mIsBreakpointEnabled = true;
    }


    public void disableBreakpoint()
    {
        mIsBreakpointEnabled = false;
    }


    @Override
    public void run()
    {
        while (mStateMachine.state() != CacheStates.STOPPED)
        {

            if (mIsBreakpointEnabled && (mStateMachine.state() == mBreakPoint))
            {
                try
                {
                    synchronized (this)
                    {
                        mLog.debug("reader paused");
                        mIsTrheadWaiting = 1;
                        this.wait();
                        mLog.debug("reader resumed");
                        mIsTrheadWaiting = 0;
                    }
                }
                catch (InterruptedException e)
                {
                    mLog.error("ignoring unexpected exception during wait", e);
                }
            }
            super.tick();
            super.breatingPause();
        }
    }


    public CacheStates state()
    {
        return mStateMachine.state();
    }


    public CacheStates state(CacheStates newState)
    {
        return mStateMachine.state(newState);
    }


    public CacheStates transit()
    {
        return mStateMachine.transit();
    }
}
