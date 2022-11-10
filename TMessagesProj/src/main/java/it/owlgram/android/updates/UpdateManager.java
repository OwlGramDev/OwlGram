package it.owlgram.android.updates;

import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.StoreUtils;
import it.owlgram.android.entities.HTMLKeeper;
import it.owlgram.android.helpers.StandardHTTPRequest;

public class UpdateManager {


    public static void isDownloadedUpdate(UpdateUICallback updateUICallback) {
        new Thread() {
            @Override
            public void run() {
                boolean result = ApkDownloader.updateDownloaded();
                AndroidUtilities.runOnUIThread(() -> updateUICallback.onResult(result));
            }
        }.start();
    }

    public interface UpdateUICallback {
        void onResult(boolean result);
    }

    public static void getChangelogs(ChangelogCallback changelogCallback) {
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new Thread() {
            @Override
            public void run() {
                try {
                    String url = String.format("https://app.owlgram.org/get_changelogs?lang=%s&version=%s", locale.getLanguage(), BuildVars.BUILD_VERSION);
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String changelog_text = obj.getString("changelogs");
                    if (!changelog_text.equals("null")) {
                        AndroidUtilities.runOnUIThread(() -> changelogCallback.onSuccess(HTMLKeeper.htmlToEntities(changelog_text, null, true)));
                    }
                } catch (Exception ignored) {
                }
            }
        }.start();
    }

    public static void checkUpdates(UpdateCallback updateCallback) {
        if (StoreUtils.isFromPlayStore()) {
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(ApplicationLoader.applicationContext);
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                int versionCode = appUpdateInfo.availableVersionCode();
                if (versionCode >= BuildVars.BUILD_VERSION) {
                    checkInternal(updateCallback, versionCode / 10);
                } else {
                    updateCallback.onError(new Exception("No updates available, current version is " + BuildVars.BUILD_VERSION + " and available version is " + versionCode));
                }
            });
            appUpdateInfoTask.addOnFailureListener(updateCallback::onError);
        } else {
            checkInternal(updateCallback, -1);
        }
    }

    private static void checkInternal(UpdateCallback updateCallback, int psVersionCode) {
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        boolean betaMode = OwlConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "unknown";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 3:
                            abi = "arm-v7a";
                            break;
                        case 2:
                        case 4:
                            abi = "x86";
                            break;
                        case 5:
                        case 7:
                            abi = "arm64-v8a";
                            break;
                        case 6:
                        case 8:
                            abi = "x86_64";
                            break;
                        case 0:
                        case 9:
                            abi = "universal";
                            break;
                    }
                    String url = String.format(locale, "https://app.owlgram.org/version?lang=%s&beta=%s&abi=%s", locale.getLanguage(), betaMode, URLEncoder.encode(abi, StandardCharsets.UTF_8.toString()));
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String update_status = obj.getString("status");
                    if (update_status.equals("no_updates")) {
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                    } else {
                        int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psVersionCode <= 0 ? obj.getInt("version") : psVersionCode);
                        if (remoteVersion > code) {
                            UpdateAvailable updateAvailable = loadUpdate(obj);
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
                        } else {
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                        }
                    }
                } catch (Exception e) {
                    AndroidUtilities.runOnUIThread(() -> updateCallback.onError(e));
                }
            }
        }.start();
    }

    public static class UpdateNotAvailable {
    }

    public static class UpdateAvailable {
        public String title;
        public String desc;
        public String note;
        public String banner;
        public String link_file;
        public int version;
        public long file_size;

        UpdateAvailable(String title, String desc, String note, String banner, String link_file, int version, long file_size) {
            this.title = title;
            this.desc = desc;
            this.note = note;
            this.banner = banner;
            this.version = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : version;
            this.link_file = link_file;
            this.file_size = file_size;
        }

        @NonNull
        @Override
        public String toString() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", title);
                obj.put("desc", desc);
                obj.put("note", note);
                obj.put("banner", banner);
                obj.put("version", version);
                obj.put("link_file", link_file);
                obj.put("file_size", file_size);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString();
        }
    }

    public static UpdateAvailable loadUpdate(JSONObject obj) throws JSONException {
        return new UpdateAvailable(obj.getString("title"), obj.getString("desc"), obj.getString("note"), obj.getString("banner"), obj.getString("link_file"), obj.getInt("version"), obj.getLong("file_size"));
    }

    public static int currentVersion() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return (pInfo.versionCode / 10);
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isAvailableUpdate() {
        return OwlConfig.updateData.length() > 0;
    }

    public interface UpdateCallback {
        void onSuccess(Object updateResult);

        void onError(Exception e);
    }

    public interface ChangelogCallback {
        void onSuccess(Pair<String, ArrayList<TLRPC.MessageEntity>> updateResult);
    }
}
