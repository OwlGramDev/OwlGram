package it.owlgram.android.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.NotificationCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.owlgram.android.OwlConfig;

public class ApkDownloader {
    @SuppressLint("StaticFieldLeak")
    private static DownloadThread downloadThread;
    private final static AppDownloader.UpdateListener []listeners;

    static {
        listeners = new AppDownloader.UpdateListener[3];
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
        boolean isAvailableFile = apkFile().exists() && downloadThread == null && !isCorrupted;
        if ((BuildVars.BUILD_VERSION >= OwlConfig.oldDownloadedVersion || OwlConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            OwlConfig.setUpdateData("");
            return false;
        }
        return isAvailableFile;
    }

    public static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath() + "/update.apk");
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void downloadAPK(Context context, String link, int version) {
        if (downloadThread != null) return;
        File output = apkFile();
        if (output.exists())
            output.delete();
        OwlConfig.saveOldVersion(version);
        downloadThread = new DownloadThread(context, output);
        downloadThread.downloadFile(link);
    }

    public static void cancel() {
        if (downloadThread != null) {
            downloadThread.cancel();
        }
    }

    public static void setDownloadListener(AppDownloader.UpdateListener listener) {
        listeners[0] = listener;
    }

    public static void setDownloadMainListener(AppDownloader.UpdateListener listener) {
        listeners[1] = listener;
    }

    public static void setDownloadDialogsListener(AppDownloader.UpdateListener listener) {
        listeners[2] = listener;
    }

    public static boolean isRunningDownload() {
        return downloadThread != null;
    }

    public static long downloadedBytes() {
        if (downloadThread != null) {
            return downloadThread.total;
        }
        return 0;
    }

    public static long totalBytes() {
        if (downloadThread != null) {
            return downloadThread.fileLength;
        }
        return 0;
    }

    public static int getDownloadProgress() {
        if (downloadThread != null) {
            return downloadThread.percentage;
        }
        return 0;
    }

    private static class DownloadThread {
        private final Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private final File mTargetFile;
        public long total = 0;
        public long fileLength = 0;
        public int percentage = 0;
        private boolean isDownloadCanceled = false;

        public DownloadThread(Context context, File targetFile) {
            this.mContext = context;
            this.mTargetFile = targetFile;
        }

        public void cancel() {
            isDownloadCanceled = true;
        }

        public void downloadFile(String link) {
            onPreExecute();
            new Thread() {
                @Override
                public void run() {
                    InputStream input = null;
                    OutputStream output = null;
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(link);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                            return;
                        }
                        fileLength = connection.getContentLength();
                        input = connection.getInputStream();
                        output = new FileOutputStream(mTargetFile, false);

                        byte[] data = new byte[4096];
                        total = 0;
                        int count;
                        long last_update = System.currentTimeMillis();
                        AndroidUtilities.runOnUIThread(() -> onProgressUpdate(0, 0, fileLength));
                        while ((count = input.read(data)) != -1) {
                            if (isDownloadCanceled) {
                                input.close();
                                AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                                return;
                            }
                            total += count;
                            if (fileLength > 0) {
                                long curr_time = System.currentTimeMillis();
                                if (curr_time - last_update > 1000) {
                                    last_update = curr_time;
                                    percentage = (int) (total * 100 / fileLength);
                                    AndroidUtilities.runOnUIThread(() -> onProgressUpdate(percentage, total, fileLength));
                                }
                            }
                            output.write(data, 0, count);
                        }
                        AndroidUtilities.runOnUIThread(() -> onProgressUpdate((int) (total * 100 / fileLength), total, fileLength));
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(false));
                    } catch (Exception e) {
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                    } finally {
                        try {
                            if (output != null)
                                output.close();
                            if (input != null)
                                input.close();
                        } catch (IOException ignored) {
                        }
                        if (connection != null)
                            connection.disconnect();
                    }
                }
            }.start();
        }

        private void onProgressUpdate(int percentage, long downBytes, long totBytes) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadProgressChanged);
            onProgressChange(percentage, downBytes, totBytes);
        }

        private void onPreExecute() {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L);
            onPreStart();
        }

        private void onPostExecute(boolean isCanceled) {
            mWakeLock.release();
            if (isCanceled) {
                deleteUpdate();
            }
            downloadThread = null;
            onFinished();
        }
    }

    private static void onPreStart() {
        for (AppDownloader.UpdateListener value : listeners) {
            if (value != null) value.onPreStart();
        }
    }

    private static void onProgressChange(int percentage, long downBytes, long totBytes) {
        for (AppDownloader.UpdateListener value : listeners) {
            if (value != null) value.onProgressChange(percentage, downBytes, totBytes);
        }
    }

    private static void onFinished() {
        for (AppDownloader.UpdateListener value : listeners) {
            if (value != null) value.onFinished();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteUpdate() {
        File file = apkFile();
        if (file.exists())
            file.delete();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadFailed);
    }
}
