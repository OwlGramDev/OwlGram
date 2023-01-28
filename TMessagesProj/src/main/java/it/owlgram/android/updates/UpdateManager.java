package it.owlgram.android.updates;

import android.app.Activity;
import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.StoreUtils;
import it.owlgram.android.entities.HTMLKeeper;
import it.owlgram.android.helpers.FileDownloadHelper;
import it.owlgram.android.helpers.StandardHTTPRequest;

public class UpdateManager {

    public static boolean checkingForChangelogs = false;

    public static void isDownloadedUpdate(UpdateUICallback updateUICallback) {
        new Thread() {
            @Override
            public void run() {
                boolean result = AppDownloader.updateDownloaded();
                AndroidUtilities.runOnUIThread(() -> updateUICallback.onResult(result));
            }
        }.start();
    }

    public interface UpdateUICallback {
        void onResult(boolean result);
    }

    public static String getApkChannel() {
        return OwlConfig.betaUpdates ? "OwlGramBeta" : "OwlGramAPKs";
    }

    public static void getChangelogs(ChangelogCallback changelogCallback) {
        if (checkingForChangelogs) return;
        checkingForChangelogs = true;
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
                } finally {
                    checkingForChangelogs = false;
                }
            }
        }.start();
    }

    public static void checkUpdates(UpdateCallback updateCallback) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.checkUpdates(new PlayStoreAPI.UpdateCheckCallback() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    checkInternal(updateCallback, appUpdateInfo);
                }

                @Override
                public void onError(Exception e) {
                    if (e instanceof PlayStoreAPI.NoUpdateException) {
                        updateCallback.onSuccess(new UpdateNotAvailable());
                    } else {
                        updateCallback.onError(e);
                    }
                }
            });
        } else {
            checkInternal(updateCallback, null);
        }
    }

    private static void checkInternal(UpdateCallback updateCallback, AppUpdateInfo psAppUpdateInfo) {
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
                    String url = String.format(locale, "https://app.owlgram.org/version?lang=%s&beta=%s&abi=%s", locale.getLanguage(), betaMode, URLEncoder.encode(abi, StandardCharsets.UTF_8.name()));
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String update_status = obj.getString("status");
                    if (update_status.equals("no_updates")) {
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                    } else {
                        int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psAppUpdateInfo != null ? PlayStoreAPI.getVersionCode(psAppUpdateInfo) : obj.getInt("version"));
                        if (remoteVersion > code) {
                            UpdateAvailable updateAvailable = loadUpdate(obj);
                            OwlConfig.saveUpdateStatus(1);
                            updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
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

        public void setPlayStoreMetaData(@Nullable AppUpdateInfo appUpdateInfo) {
            if (appUpdateInfo == null) return;
            this.file_size = appUpdateInfo.totalBytesToDownload();
            this.version = PlayStoreAPI.getVersionCode(appUpdateInfo);
        }

        public boolean isReminded() {
            return OwlConfig.remindedUpdate == version;
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

    public static boolean isAvailableUpdate() {
        boolean updateValid = false;
        String data = OwlConfig.updateData;
        try {
            if(data.length() > 0) {
                UpdateAvailable update = loadUpdate(new JSONObject(data));
                if (update.version > BuildVars.BUILD_VERSION && !update.isReminded()) {
                    updateValid = true;
                }
            }
        } catch (Exception ignored){}
        return updateValid;
    }

    public interface UpdateCallback {
        void onSuccess(Object updateResult);

        void onError(Exception e);
    }

    public interface ChangelogCallback {
        void onSuccess(Pair<String, ArrayList<TLRPC.MessageEntity>> updateResult);
    }

    public static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath() + "/update.apk");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteUpdate() {
        File file = apkFile();
        if (file.exists())
            file.delete();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadFailed);
    }

    public static boolean updateDownloaded() {
        boolean isCorrupted = true;
        try {
            String data = OwlConfig.updateData;
            if (data.length() > 0) {
                UpdateManager.UpdateAvailable update = UpdateManager.loadUpdate(new JSONObject(data));
                if (update.file_size == apkFile().length()) {
                    isCorrupted = false;
                }
            }
        } catch (Exception ignored) {
        }
        boolean isAvailableFile = apkFile().exists() && !FileDownloadHelper.isRunningDownload("appUpdate") && !isCorrupted;
        if ((BuildVars.BUILD_VERSION >= OwlConfig.oldDownloadedVersion || OwlConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            OwlConfig.setUpdateData("");
            return false;
        }
        return isAvailableFile;
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }
}
