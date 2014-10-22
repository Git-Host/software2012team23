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

package at.tugraz.ist.akm.test.testdata;

import android.content.ContentResolver;
import at.tugraz.ist.akm.trace.LogClient;

public class DefaultContactSetInserter
{

    private LogClient mLog = new LogClient(this);
    private ContentResolver mContentResolver = null;


    public DefaultContactSetInserter(ContentResolver contentProvider)
    {
        mContentResolver = contentProvider;
    }


    public void insertDefaultContacts()
    {
        try
        {
            PhonebookTestsHelper
                    .storeContacts(new DefaultContacts().getDefaultRecords(),
                            mContentResolver);
        }
        catch (Throwable e)
        {
            mLog.error("could not store contacts", e);
        }
    }


    public void clearDefaultContacts()
    {
        PhonebookTestsHelper.deleteContacts(
                new DefaultContacts().getDefaultRecords(), mContentResolver);
    }
}
