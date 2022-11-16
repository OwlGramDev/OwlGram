package it.owlgram.android.updates;

import com.google.android.exoplayer2.util.Log;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;

public class PlayStoreAPI {
    private static final AppUpdateManager appUpdateManager;
    private static AppUpdateInfo appUpdateInfo;

    static {
        appUpdateManager = AppUpdateManagerFactory.create(ApplicationLoader.applicationContext);
    }

    private static void loadUpdateInfo(UpdateInfoCallback updateInfoCallback) {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            PlayStoreAPI.appUpdateInfo = appUpdateInfo;
            Log.e("PlayStoreAPI", "appUpdateInfo: " + appUpdateInfo);
            updateInfoCallback.onSuccess();
        });
        appUpdateInfoTask.addOnFailureListener(updateInfoCallback::onError);
    }

    public static void checkUpdates(UpdateCheckCallback updateInfoCallback) {
        loadUpdateInfo(new UpdateInfoCallback() {
            @Override
            public void onSuccess() {
                int versionCode = getVersionCode();
                if (versionCode >= BuildVars.BUILD_VERSION && PlayStoreAPI.appUpdateInfo.updateAvailability() >= UpdateAvailability.UPDATE_AVAILABLE) {
                    updateInfoCallback.onSuccess(versionCode);
                } else {
                    updateInfoCallback.onError(new Exception("No updates available, current version is " + BuildVars.BUILD_VERSION + " and available version is " + versionCode));
                }
            }

            @Override
            public void onError(Exception e) {
                updateInfoCallback.onError(e);
            }
        });
    }

    private static int getVersionCode() {
        if (appUpdateInfo != null) {
            return appUpdateInfo.availableVersionCode() / 10;
        }
        return 0;
    }

    private interface UpdateInfoCallback {
        void onSuccess();

        void onError(Exception e);
    }

    public interface UpdateCheckCallback {
        void onSuccess(int versionCode);

        void onError(Exception e);
    }
}
