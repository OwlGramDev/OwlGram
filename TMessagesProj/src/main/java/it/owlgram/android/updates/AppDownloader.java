package it.owlgram.android.updates;

import android.app.Activity;

import it.owlgram.android.StoreUtils;
import it.owlgram.android.helpers.FileDownloadHelper;

public class AppDownloader {

    public static int getDownloadProgress() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.getDownloadProgress();
        } else {
            return FileDownloadHelper.getDownloadProgress("appUpdate");
        }
    }

    public static boolean isRunningDownload() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.isRunningDownload();
        } else {
            return FileDownloadHelper.isRunningDownload("appUpdate");
        }
    }

    public static boolean updateDownloaded() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.updateDownloaded();
        } else {
            return UpdateManager.updateDownloaded();
        }
    }

    public static long downloadedBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.downloadedBytes();
        } else {
            return FileDownloadHelper.downloadedBytes("appUpdate");
        }
    }

    public static long totalBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.totalBytes();
        } else {
            return FileDownloadHelper.totalBytes("appUpdate");
        }
    }

    public static void setListener(String id, UpdateListener listener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.addListener(id, listener);
        } else {
            FileDownloadHelper.addListener("appUpdate", id, new FileDownloadHelper.FileDownloadListener() {
                @Override
                public void onPreStart(String id) {
                    listener.onPreStart();
                }

                @Override
                public void onProgressChange(String id, int percentage, long downBytes, long totBytes) {
                    listener.onProgressChange(percentage, downBytes, totBytes);
                }

                @Override
                public void onFinished(String id, boolean isCanceled) {
                    listener.onFinished();
                }
            });
        }
    }

    public static void installUpdate(Activity activity) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.installUpdate();
        } else {
            UpdateManager.installUpdate(activity);
        }
    }

    public interface UpdateListener {
        void onPreStart();

        void onProgressChange(int percentage, long downBytes, long totBytes);

        void onFinished();
    }
}
