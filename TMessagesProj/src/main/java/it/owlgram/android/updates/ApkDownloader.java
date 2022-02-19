package it.owlgram.android.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.PowerManager;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
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
    private static final Object sync = new Object();
    private static boolean configLoaded;
    @SuppressLint("StaticFieldLeak")
    private static DownloadThread downloadThread;
    private static UpdateListener updateManager;
    private static UpdateListener updateMainManager;
    private static UpdateListener updateDialogsManager;

    static {
        loadDownloadInfo();
    }

    private static void loadDownloadInfo() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            downloadThread = null;
            configLoaded = true;
        }
    }

    public static boolean updateDownloaded() {
        int code = 0;
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            code = pInfo.versionCode / 10;
        } catch (Exception ignored){}
        boolean isCorrupted = true;
        try {
            String data = OwlConfig.updateData;
            if (data.length() > 0) {
                JSONObject jsonObject = new JSONObject(data);
                UpdateManager.UpdateAvailable update = UpdateManager.loadUpdate(jsonObject);
                if(update.file_size == apkFile().length()) {
                    isCorrupted = false;
                }
            }
        } catch (Exception ignored) {}
        boolean isAvailableFile = apkFile().exists() && downloadThread == null && !isCorrupted;
        if((code >= OwlConfig.oldDownloadedVersion || OwlConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            OwlConfig.setUpdateData("");
            return false;
        }
        return isAvailableFile;
    }

    public static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath()+"/update.apk");
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }

    public static void downloadAPK(Context context, String link, int version) {
        if(downloadThread != null) return;
        File output = apkFile();
        OwlConfig.saveOldVersion(version);
        downloadThread = new DownloadThread(context, output);
        downloadThread.downloadFile(link);
    }

    public static void cancel() {
        if(downloadThread != null){
            downloadThread.cancel();
        }
    }

    public static void setDownloadListener(UpdateListener u) {
        updateManager = u;
    }

    public static void setDownloadMainListener(UpdateListener u) {
        updateMainManager = u;
    }

    public static void setDownloadDialogsListener(UpdateListener u) {
        updateDialogsManager = u;
    }

    public static boolean isRunningDownload() {
        return downloadThread != null;
    }

    public static long downloadedBytes() {
        if(downloadThread != null){
            return downloadThread.total;
        }
        return 0;
    }

    public static long totalBytes() {
        if(downloadThread != null){
            return downloadThread.fileLength;
        }
        return 0;
    }

    public static int percentage() {
        if(downloadThread != null){
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
                        output = new FileOutputStream(mTargetFile,false);

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
            if(updateManager != null) {
                try {
                    updateManager.onProgressChange(percentage, downBytes, totBytes);
                }catch (Exception ignored) {}
            }
            if(updateMainManager != null) {
                try {
                    updateMainManager.onProgressChange(percentage, downBytes, totBytes);
                }catch (Exception ignored) {}
            }
            if(updateDialogsManager != null) {
                try {
                    updateDialogsManager.onProgressChange(percentage, downBytes, totBytes);
                }catch (Exception ignored) {}
            }
        }

        private void onPreExecute() {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10*60*1000L);
            if(updateManager != null) {
                updateManager.onPreStart();
            }
            if(updateMainManager != null) {
                updateMainManager.onPreStart();
            }
            if(updateDialogsManager != null) {
                updateDialogsManager.onPreStart();
            }
        }

        private void onPostExecute(boolean isCanceled) {
            mWakeLock.release();
            if (isCanceled) {
                deleteUpdate();
            }
            downloadThread = null;
            if(updateManager != null) {
                updateManager.onFinished();
            }
            if(updateMainManager != null) {
                updateMainManager.onFinished();
            }
            if(updateDialogsManager != null) {
                updateDialogsManager.onFinished();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteUpdate() {
        File file = apkFile();
        if(file.exists())
            file.delete();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadFailed);
    }

    public interface UpdateListener {
        void onPreStart();
        void onProgressChange(int percentage, long downBytes, long totBytes);
        void onFinished();
    }
}
