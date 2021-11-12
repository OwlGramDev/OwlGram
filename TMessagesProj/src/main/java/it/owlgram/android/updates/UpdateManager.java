package it.owlgram.android.updates;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Scanner;

import it.owlgram.android.OwlConfig;

public class UpdateManager {

    public static void checkUpdates(UpdateCallback updateCallback) {
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new MyAsyncTask().request(locale.getLanguage(), OwlConfig.betaUpdates, updateCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    private static class MyAsyncTask extends AsyncTask<Void, Integer, Object> {
        String language;
        Boolean isBeta;
        UpdateCallback updateCallback;

        public MyAsyncTask request(String language, Boolean isBeta, UpdateCallback updateCallback) {
            this.language = language;
            this.isBeta = isBeta;
            this.updateCallback = updateCallback;
            return this;
        }

        @Override
        protected Object doInBackground(Void... params) {
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
                        if (BuildVars.isStandaloneApp()) {
                            abi = "direct";
                        } else {
                            abi = "universal";
                        }
                        break;
                }
                String url = String.format("https://app.owlgram.org/version?lang=%s&beta=%s&abi=%s", language, isBeta,  URLEncoder.encode(abi, "utf-8"));
                JSONObject obj = new JSONObject(new Http(url).request());
                String update_status = obj.getString("status");
                if (update_status.equals("no_updates")) {
                    return new UpdateNotAvailable();
                } else {
                    int remoteVersion = obj.getInt("version");
                    if (remoteVersion > code) {
                        return loadUpdate(obj);
                    } else {
                        return new UpdateNotAvailable();
                    }
                }
            }catch (Exception e){
                return e;
            }
        }
        @Override
        protected void onPostExecute(Object result) {
            if (result == null) {
                updateCallback.onError(null);
            } else if (result instanceof Exception) {
                updateCallback.onError((Exception) result);
            } else {
                updateCallback.onSuccess(result);
            }
        }
    }

    public static class UpdateNotAvailable {}

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
            this.version = version;
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

    private static class Http {
        private final HttpURLConnection httpURLConnection;

        public Http(String url) throws IOException {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setConnectTimeout(1000);
        }

        public Http header(String key, String value) {
            httpURLConnection.setRequestProperty(key, value);
            return this;
        }

        public Http data(String data) throws IOException {
            httpURLConnection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            byte[] t = data.getBytes(Charset.defaultCharset());
            dataOutputStream.write(t);
            dataOutputStream.flush();
            dataOutputStream.close();
            return this;
        }

        public String request() throws IOException {
            httpURLConnection.connect();
            InputStream stream;
            if (httpURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                stream = httpURLConnection.getInputStream();
            } else {
                stream = httpURLConnection.getErrorStream();
            }
            return new Scanner(stream, "UTF-8")
                    .useDelimiter("\\A")
                    .next();
        }
    }

    public static int currentVersion() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return pInfo.versionCode / 10;
        } catch (Exception e){
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
}
