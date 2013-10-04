package at.tugraz.ist.akm.activities;

import android.os.Build;

public class DevelopmentSettings {

	static final boolean IS_RUNNING_ON_EMULATOR = "google_sdk".equals(Build.PRODUCT);
	
}
