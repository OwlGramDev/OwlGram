package it.owlgram.android.magic;

import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;

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
    }

    public static class SettingsExport extends MagicHashMapVector<String, Object> {

    }
}
