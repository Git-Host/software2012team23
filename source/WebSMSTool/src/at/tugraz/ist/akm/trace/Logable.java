package at.tugraz.ist.akm.trace;

public class Logable
{
    private static boolean mEnabled = false;
	private String mTag = "<notag>";

	public Logable()
	{
	}

	public Logable(String tag)
	{
		mTag = new String(tag);
	}

	public void logVerbose(String message)
	{
	    if (mEnabled)
	        logVerbose(message, null);
	}

	public void logVerbose(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.VERBOSE, mTag, message, t);
	}

	public void logError(String message)
	{
	    if (mEnabled)
	        logError(message, null);
	}

	public void logError(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.ERROR, mTag, message, t);
	}

	public void logWarning(String message)
	{
	    if (mEnabled)
	        logWarning(message, null);
	}

	public void logWarning(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.WARNING, mTag, message, t);
	}

	public void logInfo(String message)
	{
	    if (mEnabled)
	        logInfo(message, null);
	}

	public void logInfo(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.INFO, mTag, message, t);
	}

	public void logDebug(String message)
	{
	    if (mEnabled)
	        logDebug(message, null);
	}

	public void logDebug(String message, Throwable t)
	{
	    if (mEnabled)
	        Logger.log(Logger.LogLevel.DEBUG, mTag, message, t);
	}
}
