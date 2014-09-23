package at.tugraz.ist.akm.exceptional;

import java.lang.Thread.UncaughtExceptionHandler;

import at.tugraz.ist.akm.trace.LogClient;

public class UncaughtExceptionLogger
{

    LogClient mLog = null;
    UncaughtExceptionHandler mDefaultHandler = null;


    @SuppressWarnings("unused")
    private UncaughtExceptionLogger()
    {
    }


    public UncaughtExceptionLogger(LogClient logger)
    {
        mLog = logger;
    }


    public void register()
    {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        try
        {

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e)
                {
                    mLog.error(
                            "uncaught exception detected in thread["
                                    + t.getId() + "}", e);
                }
            });
        }
        catch (SecurityException e)
        {
            mLog.error("could not set default UncaughtExceptionHandler", e);
        }
    }


    public void unregister()
    {
        Thread.setDefaultUncaughtExceptionHandler(mDefaultHandler);
    }
}
