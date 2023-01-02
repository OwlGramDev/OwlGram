package it.owlgram.android.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import it.owlgram.android.OwlConfig;

public class CustomEmojiHelper {
    private static Typeface systemEmojiTypeface;
    private static boolean loadSystemEmojiFailed = false;
    private static final String EMOJI_FONT_AOSP = "NotoColorEmoji.ttf";
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("EmojiPackConfig", Context.MODE_PRIVATE);
    private static boolean loadingPack = false;
    private static final ArrayList<EmojiPackInfo> emojiPacksInfo = new ArrayList<>();

    public static Typeface getSystemEmojiTypeface() {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            try {
                Pattern p = Pattern.compile(">(.*emoji.*)</font>", Pattern.CASE_INSENSITIVE);
                BufferedReader br = new BufferedReader(new FileReader("/system/etc/fonts.xml"));
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + m.group(1));
                        if (BuildVars.DEBUG_VERSION) {
                            FileLog.d("emoji font file fonts.xml = " + m.group(1));
                        }
                        break;
                    }
                }
                br.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (systemEmojiTypeface == null) {
                try {
                    systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + EMOJI_FONT_AOSP);
                    if (BuildVars.DEBUG_VERSION) {
                        FileLog.d("emoji font file = " + EMOJI_FONT_AOSP);
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                    loadSystemEmojiFailed = true;
                }
            }
        }
        return systemEmojiTypeface;
    }

    public static String getSelectedPackName() {
        return emojiPacksInfo
                .stream()
                .filter(emojiPackInfo -> Objects.equals(emojiPackInfo.getPackId(), OwlConfig.emojiPackSelected))
                .findFirst()
                .map(EmojiPackInfo::getPackName)
                .orElse("Apple");
    }

    public static boolean loadedPackInfo() {
        return !emojiPacksInfo.isEmpty();
    }

    public static void loadEmojisInfo() {
        if (loadingPack) {
            return;
        }
        loadingPack = true;
        emojiPacksInfo.clear();
        new Thread() {
            @Override
            public void run() {
                try {
                    String json = preferences.getString("emoji_packs", null);
                    if (json == null || System.currentTimeMillis() - getLastUpdate() > 10 * 60 * 1000) {
                        json = new StandardHTTPRequest("https://app.owlgram.org/emoji_packs").request();
                        preferences.edit().putString("emoji_packs", json).apply();
                        updateLastUpdate();
                    }
                    emojiPacksInfo.addAll(loadFromJson(json));
                } catch (Exception ignored) {
                    SystemClock.sleep(1000);
                } finally {
                    loadingPack = false;
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiPacksLoaded));
                }
            }
        }.start();
    }

    private static Long getLastUpdate() {
        return preferences.getLong("last_update", 0);
    }

    private static void updateLastUpdate() {
        preferences.edit().putLong("last_update", System.currentTimeMillis()).apply();
    }

    private static ArrayList<EmojiPackInfo> loadFromJson(String json) throws JSONException {
        ArrayList<EmojiPackInfo> packs = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            packs.add(new EmojiPackInfo(
                    obj.getString("name"),
                    obj.getString("file"),
                    obj.getString("preview"),
                    obj.getString("id"),
                    obj.getLong("file_size")
            ));
        }
        return packs.stream()
                .sorted(Comparator.comparing(EmojiPackInfo::getPackName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<EmojiPackInfo> getEmojiPacksInfo() {
        return emojiPacksInfo;
    }

    public static class EmojiPackInfo {
        private final String packName;
        private final String fileLink;
        private final String previewLink;
        private final String packId;
        private final Long packSize;

        public EmojiPackInfo(String packName, String fileLink, String previewLink, String packId, Long packSize) {
            this.packName = packName;
            this.fileLink = fileLink;
            this.previewLink = previewLink;
            this.packId = Objects.equals(packId, "apple") ? "default" : packId;
            this.packSize = packSize;
        }

        public String getPackName() {
            return packName;
        }

        public String getFileLink() {
            return fileLink;
        }

        public String getPreviewLink() {
            return previewLink;
        }

        public String getPackId() {
            return packId;
        }

        public Long getPackSize() {
            return packSize;
        }
    }
}
