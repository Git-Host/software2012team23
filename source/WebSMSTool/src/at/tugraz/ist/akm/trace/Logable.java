package at.tugraz.ist.akm.trace;

public class Logable
{

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
		logVerbose(message, null);
	}

	public void logVerbose(String message, Throwable t)
	{
		Logger.log(Logger.LogLevel.VERBOSE, mTag, message, t);
	}

	public void logError(String message)
	{
		logError(message, null);
	}

	public void logError(String message, Throwable t)
	{
		Logger.log(Logger.LogLevel.ERROR, mTag, message, t);
	}

	public void logWarning(String message)
	{
		logWarning(message, null);
	}

	public void logWarning(String message, Throwable t)
	{
		Logger.log(Logger.LogLevel.WARNING, mTag, message, t);
	}

	public void logInfo(String message)
	{
		logInfo(message, null);
	}

	public void logInfo(String message, Throwable t)
	{
		Logger.log(Logger.LogLevel.INFO, mTag, message, t);
	}

	public void logDebug(String message)
	{
		logDebug(message, null);
	}

	public void logDebug(String message, Throwable t)
	{
		Logger.log(Logger.LogLevel.DEBUG, mTag, message, t);
	}
}
