package it.owlgram.android.magic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.updates.PlayStoreAPI;

public class OWLENC {
    public static class DrawerItems extends MagicVector<String> {
        public void migrate(String input) {
            try {
                JSONArray items = new JSONArray(input);
                for (int i = 0; i < items.length(); i++) {
                    add(items.getString(i));
                }
            } catch (JSONException ignored){}
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c421;
        }
    }

    public static class ExcludedLanguages extends MagicHashVector<String> {
        public void migrate(String input) {
            try {
                JSONArray languages = new JSONArray(input);
                for (int i = 0; i < languages.length(); i++) {
                    add(languages.getString(i));
                }
            } catch (JSONException ignored){}
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c422;
        }
    }

    public static class UpdateAvailable extends MagicBaseObject {
        public String title;
        public String description;
        public String note;
        public String banner;
        public int version;
        public String fileLink;
        public long fileSize;

        public UpdateAvailable(JSONObject object) throws JSONException {
            fromJSON(object);
        }

        public UpdateAvailable() {}

        public void fromJSON(JSONObject updateInfo) throws JSONException {
            title = updateInfo.getString("title");
            description = updateInfo.getString("desc");
            note = updateInfo.getString("note");
            banner = updateInfo.getString("banner");
            version = updateInfo.getInt("version");
            fileLink = updateInfo.getString("link_file");
            fileSize = updateInfo.getLong("file_size");
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c419;
        }

        public void setPlayStoreMetaData(@Nullable AppUpdateInfo appUpdateInfo) {
            if (appUpdateInfo == null) return;
            this.fileSize = appUpdateInfo.totalBytesToDownload();
            this.version = PlayStoreAPI.getVersionCode(appUpdateInfo);
        }

        public boolean isReminded() {
            return OwlConfig.remindedUpdate == version;
        }
    }

    public static class LanguagePacksVersions extends MagicHashMapVector<String, String> {
        public void migrate(String input) {
            try {
                fromJson(input);
            } catch (JSONException ignored){}
        }

        public void fromJson(String input) throws JSONException {
            JSONObject languages = new JSONObject(input);
            for (String key : keySet()) {
                languages.put(key, get(key));
            }
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c423;
        }
    }

    public static class ConfirmSending extends MagicBaseObject {
        public boolean sendGifs;
        public boolean sendStickers;
        public boolean sendAudio;
        public boolean sendVideo;


        public void toggleGifs() {
            sendGifs ^= true;
        }

        public void toggleStickers() {
            sendStickers ^= true;
        }

        public void toggleAudio() {
            sendAudio ^= true;
        }

        public void toggleVideo() {
            sendVideo ^= true;
        }

        public int count() {
            int count = 0;
            if (sendGifs) count++;
            if (sendStickers) count++;
            if (sendAudio) count++;
            if (sendVideo) count++;
            return count;
        }

        public void setAll(boolean value) {
            sendGifs = value;
            sendStickers = value;
            sendAudio = value;
            sendVideo = value;
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c424;
        }
    }

    public static class SettingsBackup extends MagicBaseObject implements Iterable<String> {
        private final HashMap<String, Object> settings = new HashMap<>();
        public int VERSION;

        public boolean contains(String key) {
            return settings.containsKey(key);
        }

        public Object get(String key) {
            return settings.get(key);
        }

        public void put(String key, Object value) {
            settings.put(key, value);
        }

        public int size() {
            return settings.size();
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c420;
        }

        @NonNull
        @Override
        public Iterator<String> iterator() {
            return settings.keySet().iterator();
        }

        public boolean isNotLegacy(PushbackInputStream stream) throws IOException {
            byte[] fileBytes = new byte[stream.available()];
            int len = stream.read(fileBytes);
            stream.unread(fileBytes, 0, len);
            try {
                JSONObject jsonObject = new JSONObject(new String(fileBytes, StandardCharsets.UTF_8));
                VERSION = jsonObject.getInt("DB_VERSION");
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.equals("DB_VERSION") || key.equals("sendConfirm")) continue;
                    if (key.equals("drawerItems")) {
                        DrawerItems drawerItems = new DrawerItems();
                        drawerItems.migrate(jsonObject.getString(key));
                        settings.put(key, drawerItems);
                        continue;
                    }
                    settings.put(key, jsonObject.get(key));
                }
                ConfirmSending confirmSending = new ConfirmSending();
                confirmSending.sendStickers = confirmSending.sendGifs = jsonObject.getBoolean("confirmStickersGIFs");
                confirmSending.sendAudio = confirmSending.sendVideo = jsonObject.getBoolean("sendConfirm");
                settings.put("confirmSending", confirmSending);
            } catch (Exception e) {
                return true;
            }
            return false;
        }
    }
}
