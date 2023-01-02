package it.owlgram.android.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.updates.AppDownloader;

public class FileDownloadHelper {
    @SuppressLint("StaticFieldLeak")
    private static final HashMap<String, DownloadThread> downloadThreads = new HashMap<>();
    private final static HashMap<String, AppDownloader.UpdateListener> listeners = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void downloadFile(Context context, File output, String link, int version) {
        String abs = output.getAbsolutePath();
        if (downloadThreads.get(abs) != null) return;
        if (output.exists())
            output.delete();
        OwlConfig.saveOldVersion(version);
        DownloadThread downloadThread = new DownloadThread(context, output);
        downloadThread.downloadFile(link);
        downloadThreads.put(abs, downloadThread);
    }

    public static void cancel(File output) {
        DownloadThread downloadThread = downloadThreads.get(output.getAbsolutePath());
        if (downloadThread != null) {
            downloadThread.cancel();
        }
    }

    public static boolean isRunningDownload(File output) {
        return downloadThreads.get(output.getAbsolutePath()) != null;
    }

    public static long downloadedBytes(File output) {
        DownloadThread downloadThread = downloadThreads.get(output.getAbsolutePath());
        if (downloadThread != null) {
            return downloadThread.total;
        }
        return 0;
    }

    public static long totalBytes(File output) {
        DownloadThread downloadThread = downloadThreads.get(output.getAbsolutePath());
        if (downloadThread != null) {
            return downloadThread.fileLength;
        }
        return 0;
    }

    public static int getDownloadProgress(File output) {
        DownloadThread downloadThread = downloadThreads.get(output.getAbsolutePath());
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

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void onPostExecute(boolean isCanceled) {
            mWakeLock.release();
            if (isCanceled) {
               mTargetFile.delete();
            }
            downloadThreads.remove(mTargetFile.getAbsolutePath());
            onFinished();
        }
    }

    public static void addListener(String key, AppDownloader.UpdateListener listener) {
        listeners.put(key, listener);
    }

    private static void onPreStart() {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onPreStart();
        }
    }

    private static void onProgressChange(int percentage, long downBytes, long totBytes) {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onProgressChange(percentage, downBytes, totBytes);
        }
    }

    private static void onFinished() {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onFinished();
        }
    }
}
