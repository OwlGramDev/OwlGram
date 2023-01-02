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
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;

import java.io.BufferedReader;
import java.io.File;
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
    private final static String EMOJI_PACKS_CACHE_DIR = AndroidUtilities.getCacheDir().getAbsolutePath() + "/emojis/";
    private static final Runnable invalidateUiRunnable = () -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiLoaded);

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
        loadEmojisInfo(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiPacksLoaded));
    }

    public static void loadEmojisInfo(EmojiPackListener listener) {
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
                    AndroidUtilities.runOnUIThread(listener::onLoaded);
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
                    obj.getLong("file_size"),
                    obj.getInt("version")
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
        private final Integer packVersion;

        public EmojiPackInfo(String packName, String fileLink, String previewLink, String packId, Long packSize, Integer packVersion) {
            this.packName = packName;
            this.fileLink = fileLink;
            this.previewLink = previewLink;
            this.packId = Objects.equals(packId, "apple") ? "default" : packId;
            this.packSize = packSize;
            this.packVersion = packVersion;
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

        public Integer getPackVersion() {
            return packVersion;
        }
    }

    public static boolean isInstalledOldVersion(String emojiID, int version) {
        return getAllVersions(emojiID, version).size() > 0;
    }

    public static boolean isInstalledOffline(String emojiID) {
        return getAllVersions(emojiID, -1).size() > 0;
    }

    public static ArrayList<File> getAllVersions(String emojiID) {
        return getAllVersions(emojiID, -1);
    }

    public static ArrayList<File> getAllEmojis() {
        ArrayList<File> emojis = new ArrayList<>();
        File emojiDir = new File(EMOJI_PACKS_CACHE_DIR);
        if (emojiDir.exists()) {
            File[] files = emojiDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        emojis.add(file);
                    }
                }
            }
        }
        return emojis;
    }

    public static ArrayList<File> getAllVersions(String emojiID, int version) {
        return getAllEmojis().stream()
                .filter(file -> file.getName().startsWith(emojiID))
                .filter(file -> version == -1 || !file.getName().endsWith("_v" + version))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Long getEmojiSize() {
        return getAllEmojis().stream()
                .filter(file -> !file.getName().startsWith(OwlConfig.emojiPackSelected))
                .map(CustomEmojiHelper::calculateFolderSize)
                .reduce(0L, Long::sum);
    }

    private static long calculateFolderSize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += calculateFolderSize(file);
                }
            }
        }
        return length;
    }

    public static void deleteAll() {
        getAllEmojis().stream()
                .filter(file -> !file.getName().startsWith(OwlConfig.emojiPackSelected))
                .forEach(FileUnzipHelper::deleteFolder);
    }

    public static void deleteOldVersions(String emojiID, int version) {
        for (File oldVersion : getAllVersions(emojiID, version)) {
           FileUnzipHelper.deleteFolder(oldVersion);
        }
    }

    public static File emojiDir(String emojiID, int version) {
        return new File(EMOJI_PACKS_CACHE_DIR + emojiID + "_v" + version);
    }

    public static File emojiTmp(String emojiID) {
        return new File(EMOJI_PACKS_CACHE_DIR + emojiID + ".zip");
    }

    public static boolean emojiTmpDownloaded(String id) {
        boolean isCorrupted = true;
        try {
            long neededLength = getEmojiPacksInfo()
                    .stream()
                    .filter(emojiPackInfo -> Objects.equals(emojiPackInfo.getPackId(), id))
                    .findFirst()
                    .map(EmojiPackInfo::getPackSize)
                    .orElse(0L);
            if (emojiTmp(id).length() == neededLength && neededLength != 0) {
                isCorrupted = false;
            }
        } catch (Exception ignored) {
        }
        return emojiTmp(id).exists() && !FileDownloadHelper.isRunningDownload(id) && !isCorrupted;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkDirs() {
        File emojiDir = new File(EMOJI_PACKS_CACHE_DIR);
        if (!emojiDir.exists()) {
            emojiDir.mkdirs();
        }
    }

    public interface EmojiPackListener {
        void onLoaded();
    }

    public static void checkEmojiPacks() {
        loadEmojisInfo(() -> {
            if (emojiPacksInfo.isEmpty()) {
                if (!isInstalledOffline(OwlConfig.emojiPackSelected)) {
                    OwlConfig.emojiPackSelected = "default";
                }
                Emoji.reloadEmoji();
                AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                return;
            }
            for (EmojiPackInfo emojiPackInfo : emojiPacksInfo) {
                boolean isUpdate = isInstalledOldVersion(emojiPackInfo.getPackId(), emojiPackInfo.getPackVersion());
                if (!emojiDir(emojiPackInfo.packId, emojiPackInfo.packVersion).exists() && OwlConfig.emojiPackSelected.equals(emojiPackInfo.packId)) {
                    CustomEmojiHelper.mkDirs();
                    FileDownloadHelper.downloadFile(ApplicationLoader.applicationContext, emojiPackInfo.packId, CustomEmojiHelper.emojiTmp(emojiPackInfo.packId), emojiPackInfo.fileLink);
                    FileDownloadHelper.addListener(emojiPackInfo.packId, "checkListener", new FileDownloadHelper.FileDownloadListener() {
                        @Override
                        public void onPreStart(String id) {}

                        @Override
                        public void onProgressChange(String id, int percentage, long downBytes, long totBytes) {}

                        @SuppressWarnings("ResultOfMethodCallIgnored")
                        @Override
                        public void onFinished(String id) {
                            if (CustomEmojiHelper.emojiTmpDownloaded(id)) {
                                FileUnzipHelper.unzipFile(ApplicationLoader.applicationContext, id, CustomEmojiHelper.emojiTmp(id), CustomEmojiHelper.emojiDir(id, emojiPackInfo.packVersion));
                                FileUnzipHelper.addListener(id, "checkListener", id1 -> {
                                    CustomEmojiHelper.emojiTmp(id).delete();
                                    if (CustomEmojiHelper.emojiDir(id, emojiPackInfo.packVersion).exists()) {
                                        deleteOldVersions(emojiPackInfo.getPackId(), emojiPackInfo.getPackVersion());
                                    }
                                    Emoji.reloadEmoji();
                                    AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                                    AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                                });
                            } else {
                                CustomEmojiHelper.emojiTmp(id).delete();
                                if (!isUpdate) OwlConfig.setEmojiPackSelected("default");
                                Emoji.reloadEmoji();
                                AndroidUtilities.cancelRunOnUIThread(invalidateUiRunnable);
                                AndroidUtilities.runOnUIThread(invalidateUiRunnable);
                            }
                        }
                    });
                    break;
                }
            }
        });
    }

    public static EmojiPackInfo getEmojiPackInfo(String emojiPackId) {
        return emojiPacksInfo.stream()
                .filter(emojiPackInfo -> emojiPackInfo.getPackId().equals(emojiPackId))
                .findFirst()
                .orElse(null);
    }
}
