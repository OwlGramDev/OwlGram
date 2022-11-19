package it.owlgram.android.updates;

import android.app.Activity;

import it.owlgram.android.StoreUtils;

public class AppDownloader {
    public static void setDownloadMainListener(UpdateListener downloadMainListener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.setDownloadMainListener(downloadMainListener);
        } else {
            ApkDownloader.setDownloadMainListener(downloadMainListener);
        }
    }

    public static int getDownloadProgress() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.getDownloadProgress();
        } else {
            return ApkDownloader.getDownloadProgress();
        }
    }

    public static boolean isRunningDownload() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.isRunningDownload();
        } else {
            return ApkDownloader.isRunningDownload();
        }
    }

    public static boolean updateDownloaded() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.updateDownloaded();
        } else {
            return ApkDownloader.updateDownloaded();
        }
    }

    public static long downloadedBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.downloadedBytes();
        } else {
            return ApkDownloader.downloadedBytes();
        }
    }

    public static long totalBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.totalBytes();
        } else {
            return ApkDownloader.totalBytes();
        }
    }

    public static void setDownloadListener(UpdateListener downloadListener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.setDownloadListener(downloadListener);
        } else {
            ApkDownloader.setDownloadListener(downloadListener);
        }
    }

    public static void setDownloadDialogsListener(UpdateListener listener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.setDownloadDialogsListener(listener);
        } else {
            ApkDownloader.setDownloadDialogsListener(listener);
        }
    }

    public static void installUpdate(Activity activity) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.installUpdate();
        } else {
            ApkDownloader.installUpdate(activity);
        }
    }

    public interface UpdateListener {
        void onPreStart();

        void onProgressChange(int percentage, long downBytes, long totBytes);

        void onFinished();
    }
}
