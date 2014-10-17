package at.tugraz.ist.akm.environment;

import android.os.Build;

public class AppEnvironment
{

    public static final boolean isRunningOnEmulator()
    {
        return ("google_sdk".equals(Build.PRODUCT) || "sdk_x86"
                .equals(Build.PRODUCT));
    }
}
