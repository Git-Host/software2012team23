package at.tugraz.ist.akm.test.phonebook.contact;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;
import at.tugraz.ist.akm.trace.LogClient;

public class TestableCachedAsyncPhonebookReader extends
        CachedAsyncPhonebookReader
{
    private int mIsTrheadWaiting = -1;
    private LogClient mLog = new LogClient(this);

    private StateMachine mBreakPoint = StateMachine.READY_FOR_CHANGES;


    TestableCachedAsyncPhonebookReader(ContactFilter filter, Context context,
            IContactReader contactReader)
    {
        super(filter, context, contactReader);
    }


    public boolean isThreadWaiting()
    {
        return (mIsTrheadWaiting == 1);
    }


    public void setNextBreakpoint(StateMachine breakPoint)
    {
        mBreakPoint = breakPoint;
    }


    @Override
    public void run()
    {
        while (StateMachine.state() != StateMachine.STOPPED)
        {

            if (StateMachine.state() == mBreakPoint)
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
                    mLog.error("unexpected exception during wait", e);
                }
            }
            super.tick();
        }
    }

}
