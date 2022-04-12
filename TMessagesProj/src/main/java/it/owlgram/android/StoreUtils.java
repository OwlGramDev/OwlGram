package it.owlgram.android;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;

public class StoreUtils {
    public static boolean isDownloadedFromAnyStore() {
        return isFromPlayStore() || isFromHuaweiStore();
    }

    public static boolean isFromPlayStore() {
        return "com.android.vending".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));
    }

    public static boolean isFromHuaweiStore() {
        return "com.huawei.appmarket".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));
    }
}
