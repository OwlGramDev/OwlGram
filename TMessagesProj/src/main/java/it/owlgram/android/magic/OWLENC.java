package it.owlgram.android.magic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof DrawerItems) {
                DrawerItems other = (DrawerItems) obj;
                if (size() != other.size()) return false;
                for (int i = 0; i < size(); i++) {
                    if (!get(i).equals(other.get(i))) return false;
                }
                return true;
            }
            return false;
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
    }
}
