package at.tugraz.ist.akm.test.phonebook.contact;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.contact.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;

public class TestableCachedAsyncPhonebookReader extends
        CachedAsyncPhonebookReader
{

    private StateMachine mBreakPoint = StateMachine.READY_FOR_CHANGES;
    
    TestableCachedAsyncPhonebookReader(ContactFilter filter,
            Context context, IContactReader contactReader)
    {
        super(filter, context, contactReader);
    }
    
    
    public void setNextBreakpoint(StateMachine breakPoint)
    {
        mBreakPoint = breakPoint;
    }
    
    @Override
    public void run()
    {
        if (StateMachine.state() == mBreakPoint) {
            try {
                wait();
            } catch (InterruptedException e) { // don't care
            }
        }
        super.run();
    }
    
}
