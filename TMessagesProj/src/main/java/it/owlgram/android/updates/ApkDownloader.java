package it.owlgram.android.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
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
    private static DownloadTask downloadTask;
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
            downloadTask = null;
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
        boolean isAvailableFile = apkFile().exists() && downloadTask == null && !isCorrupted;
        if((code >= OwlConfig.oldDownloadedVersion || OwlConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            OwlConfig.setUpdateData("");
            return false;
        }
        return isAvailableFile;
    }

    private static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath()+"/update.apk");
    }

    public static void installUpdate(Activity activity) {
        AndroidUtilities.openForView(ApkDownloader.apkFile(), "update.apk", "application/vnd.android.package-archive", activity, null);
    }

    public static void downloadAPK(Context context, String link, int version) {
        if(downloadTask != null) return;
        File output = apkFile();
        OwlConfig.saveOldVersion(version);
        downloadTask = new DownloadTask(context, output);
        downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,link);
    }

    public static void cancel() {
        if(downloadTask != null){
            downloadTask.cancel(true);
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
        return downloadTask != null;
    }

    public static long downloadedBytes() {
        if(downloadTask != null){
            return downloadTask.total;
        }
        return 0;
    }

    public static long totalBytes() {
        if(downloadTask != null){
            return downloadTask.fileLength;
        }
        return 0;
    }

    public static int percentage() {
        if(downloadTask != null){
            return downloadTask.percentage;
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    private static class DownloadTask extends AsyncTask<String, Object, String> {
        @SuppressLint("StaticFieldLeak")
        private final Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private final File mTargetFile;
        public long total = 0;
        public long fileLength = 0;
        public int percentage = 0;

        public DownloadTask(Context context, File targetFile) {
            this.mContext = context;
            this.mTargetFile = targetFile;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream(mTargetFile,false);

                byte[] data = new byte[4096];
                total = 0;
                int count;
                long last_update = System.currentTimeMillis();
                publishProgress(0, 0, fileLength);
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        long curr_time = System.currentTimeMillis();
                        if (curr_time - last_update > 1000) {
                            last_update = curr_time;
                            percentage = (int) (total * 100 / fileLength);
                            publishProgress(percentage, total, fileLength);
                        }
                    }
                    output.write(data, 0, count);
                }
                publishProgress((int) (total * 100 / fileLength), total, fileLength);
            } catch (Exception e) {
                return e.toString();
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
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

        @Override
        protected void onProgressUpdate(Object... progress) {
            super.onProgressUpdate(progress);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadProgressChanged);
            if(updateManager != null) {
                try {
                    updateManager.onProgressChange((int) progress[0], (long) progress[1], (long) progress[2]);
                }catch (Exception ignored) {}
            }
            if(updateMainManager != null) {
                try {
                    updateMainManager.onProgressChange((int) progress[0], (long) progress[1], (long) progress[2]);
                }catch (Exception ignored) {}
            }
            if(updateDialogsManager != null) {
                try {
                    updateDialogsManager.onProgressChange((int) progress[0], (long) progress[1], (long) progress[2]);
                }catch (Exception ignored) {}
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mWakeLock.release();
            deleteUpdate();
            downloadTask = null;
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

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            downloadTask = null;
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
