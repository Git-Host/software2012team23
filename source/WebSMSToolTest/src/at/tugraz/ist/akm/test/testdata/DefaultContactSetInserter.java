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
