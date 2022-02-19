package it.owlgram.android;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;

public class PlayStoreUtils {
    public static boolean isDownloadedFromPlayStore() {
        return "com.android.vending".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));
    }
}
