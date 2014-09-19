package at.tugraz.ist.akm.phonebook.PhonebookCache;

public class CacheStateMachine
{

    CacheStates mCurrentState = CacheStates.ALIVE;


    public CacheStates transit()
    {
        return state(mCurrentState.nextState());
    }


    public CacheStates reset()
    {
        return state(CacheStates.ALIVE);
    }


    public CacheStates state(CacheStates aState)
    {
        return mCurrentState = aState;
    }


    public CacheStates state()
    {
        return mCurrentState;
    }
}
