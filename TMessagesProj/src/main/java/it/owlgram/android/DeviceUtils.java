package it.owlgram.android;

import android.os.Build;

public class DeviceUtils {
    public static boolean isSamsung() {
        return Build.MANUFACTURER.contains("samsung");
    }
}
