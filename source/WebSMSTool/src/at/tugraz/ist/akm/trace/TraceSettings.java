package at.tugraz.ist.akm.trace;

import android.os.Build;

public class TraceSettings {
	static final boolean ENABLE_TRACE = true;
	//static final boolean ENABLE_TRACE = "google_sdk".equals(Build.PRODUCT);
	//static final boolean ENABLE_TRACE = (ApplicationInfo.FLAG_DEBUGGABLE == 1);
	
	static final boolean ENABLE_TRACE_ERROR = true;
	static final boolean ENABLE_TRACE_WARNING = true;
	static final boolean ENABLE_TRACE_INFO = true;
	static final boolean ENABLE_TRACE_DEBUG = true;
	static final boolean ENABLE_TRACE_VERBOSE = true;
}
