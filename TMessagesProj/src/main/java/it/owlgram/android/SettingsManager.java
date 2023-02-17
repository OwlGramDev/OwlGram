package it.owlgram.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import it.owlgram.android.camera.CameraXUtils;
import it.owlgram.android.components.FileSettingsNameDialog;
import it.owlgram.android.helpers.MenuOrderManager;
import it.owlgram.android.helpers.SharedPreferencesHelper;
import it.owlgram.android.translator.Translator;

public class SettingsManager extends SharedPreferencesHelper {
    protected static boolean configLoaded;

    public static int DB_VERSION = 0;
    public static final int INVALID_CONFIGURATION = 0;
    public static final int VALID_CONFIGURATION = 1;
    public static final int NEED_UPDATE_CONFIGURATION = 2;

    //UI CHANGES
    public static final int NEED_RECREATE_FORMATTERS = 1;
    public static final int NEED_RECREATE_SHADOW = 2;
    public static final int NEED_FRAGMENT_REBASE_WITH_LAST = 4;
    public static final int NEED_FRAGMENT_REBASE = 8;
    public static final int NEED_UPDATE_CAMERAX = 16;
    public static final int NEED_UPDATE_EMOJI = 32;

    private static boolean isBackupAvailable(String key) {
        switch (key) {
            case "owlEasterSound":
            case "isChineseUser":
            case "verifyLinkTip":
            case "updateData":
            case "oldBuildVersion":
            case "languagePackVersioning":
            case "xiaomiBlockedInstaller":
            case "lastUpdateStatus":
            case "remindedUpdate":
            case "oldDownloadedVersion":
            case "lastUpdateCheck":
            case "translationTarget":
            case "doNotTranslateLanguages":
            case "translationKeyboardTarget":
            case "deepLFormality":
            case "iconStyleSelected":
            case "useMonetIcon":
            case "DB_VERSION":
            case "emojiPackSelected":
            case "NEED_RECREATE_FORMATTERS":
            case "NEED_RECREATE_SHADOW":
            case "NEED_FRAGMENT_REBASE_WITH_LAST":
            case "NEED_FRAGMENT_REBASE":
            case "INVALID_CONFIGURATION":
            case "VALID_CONFIGURATION":
            case "NEED_UPDATE_CONFIGURATION":
            case "TAB_TYPE_TEXT":
            case "TAB_TYPE_ICON":
            case "TAB_TYPE_MIX":
            case "DOWNLOAD_BOOST_DEFAULT":
            case "DOWNLOAD_BOOST_FAST":
            case "DOWNLOAD_BOOST_EXTREME":
            case "NEED_UPDATE_CAMERAX":
            case "NEED_UPDATE_EMOJI":
            case "lastSelectedCompression":
                return false;
            default:
                return true;
        }
    }

    private static boolean isNotDeprecatedConfig(String key) {
        switch (key) {
            case "showFireworks":
            case "loopWebMStickers":
            case "useCameraX":
            case "stickerSize":
            case "stickerSize2":
            case "translationTarget":
            case "swipeToPiP":
            case "configLoaded":
            case "unlimitedFavoriteStickers":
            case "unlimitedPinnedDialogs":
            case "increaseAudioMessages":
            case "useMonetIcon":
            case "scrollableChatPreview":
            case "showInActionBar":
            case "cameraXFps":
            case "stickersAutoReorder":
            case "disableStickersAutoReorder":
            case "NEED_UPDATE_CAMERAX":
            case "emojiPackSelected":
            case "disableAppBarShadow":
                return false;
            default:
                return true;
        }
    }

    public static ArrayList<String> getDifferenceBetweenCurrentConfig(MessageObject selectedObject) {
        ArrayList<String> listDifference = new ArrayList<>();
        File locFile = getSettingFileFromMessage(selectedObject);
        FileInputStream stream = null;
        Map<String, ?> listPreferences = getAll();
        try {
            stream = new FileInputStream(locFile);
            JSONObject jsonObject = new JSONObject(new Scanner(stream, "UTF-8")
                    .useDelimiter("\\A")
                    .next());
            Field[] fields = OwlConfig.class.getFields();
            for (Field field : fields) {
                String keyFound = field.getName();
                if (isBackupAvailable(keyFound)) {
                    if (jsonObject.has(keyFound)) {
                        Object result = jsonObject.get(keyFound);
                        if (!result.toString().equals(Objects.requireNonNull(field.get(Object.class)).toString())) {
                            listDifference.add(keyFound);
                        }
                    } else if (listPreferences.containsKey(keyFound)) {
                        listDifference.add(keyFound);
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return listDifference;
    }

    private static File getSettingFileFromMessage(MessageObject selectedObject) {
        File locFile = null;
        if (!TextUtils.isEmpty(selectedObject.messageOwner.attachPath)) {
            File f = new File(selectedObject.messageOwner.attachPath);
            if (f.exists()) {
                locFile = f;
            }
        }
        if (locFile == null) {
            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(selectedObject.messageOwner);
            if (f.exists()) {
                locFile = f;
            }
        }
        return locFile;
    }

    private static boolean isValidParameter(String key, Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if ("drawerItems".equals(key)) {
                try {
                    JSONArray data = new JSONArray(stringValue);
                    JSONObject object = new JSONObject();
                    for (int i = 0; i < data.length(); i++) {
                        if (data.get(i) instanceof String) {
                            String subKey = (String) data.get(i);
                            if (!object.has(subKey)) {
                                boolean foundValid = false;
                                for (String item : MenuOrderManager.list_items) {
                                    if (item.equals(subKey) || subKey.equals(MenuOrderManager.DIVIDER_ITEM)) {
                                        object.put(subKey, true);
                                        foundValid = true;
                                        break;
                                    }
                                }
                                if (!foundValid) return false;
                            }
                        }
                    }
                    return true;
                } catch (Exception ignored) {
                }
            }
        } else if (value instanceof Integer) {
            int integerValue = (int) value;
            switch (key) {
                case "deepLFormality":
                case "tabMode":
                case "cameraType":
                case "dcStyleType":
                case "downloadSpeedBoost":
                    return integerValue >= 0 && integerValue <= 2;
                case "translationProvider":
                    return integerValue == Translator.PROVIDER_GOOGLE ||
                            integerValue == Translator.PROVIDER_YANDEX ||
                            integerValue == Translator.PROVIDER_DEEPL ||
                            integerValue == Translator.PROVIDER_NIU ||
                            integerValue == Translator.PROVIDER_DUCKDUCKGO ||
                            integerValue == Translator.PROVIDER_TELEGRAM;
                case "blurIntensity":
                    return integerValue >= 0 && integerValue <= 100;
                case "eventType":
                case "buttonStyleType":
                    return integerValue >= 0 && integerValue <= 5;
                case "idType":
                case "translatorStyle":
                    return integerValue >= 0 && integerValue <= 1;
                case "cameraResolution":
                    return true;
                case "maxRecentStickers":
                    return integerValue >= 20 && integerValue <= 200;
                case "stickerSizeStack":
                    return integerValue >= 2 && integerValue <= 20;
                case "unlockedSecretIcon":
                    return integerValue >= -1 && integerValue <= 4;
            }
        }
        return value instanceof Boolean;
    }

    public static int isValidFileSettings(MessageObject selectedObject) {
        File locFile = getSettingFileFromMessage(selectedObject);
        if (locFile != null && locFile.length() <= 1024 * 25) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(locFile);
                JSONObject jsonObject = new JSONObject(new Scanner(stream, "UTF-8")
                        .useDelimiter("\\A")
                        .next());
                int foundValues = 0;
                int foundValidValues = 0;
                Field[] fields = OwlConfig.class.getFields();
                for (Field field : fields) {
                    String keyFound = field.getName();
                    if (jsonObject.has(keyFound)) {
                        Object result = jsonObject.get(keyFound);
                        String typeCheck = field.getType().getSimpleName().toLowerCase().replace("float", "int");
                        String originalCheck = result.getClass().getSimpleName().toLowerCase().replace("integer", "int");
                        foundValues++;
                        if (typeCheck.equals(originalCheck)) {
                            foundValidValues++;
                        }
                    }
                }
                for (Iterator<String> data = jsonObject.keys(); data.hasNext(); ) {
                    String key = data.next();
                    boolean foundValid = false;
                    for (Field field : fields) {
                        String keyFound = field.getName();
                        if ((key.equals(keyFound) && isValidParameter(key, jsonObject.get(key))) || !isNotDeprecatedConfig(key) || key.equals("DB_VERSION")) {
                            foundValid = true;
                            break;
                        }
                    }
                    if (!foundValid) {
                        foundValidValues--;
                        break;
                    }
                }
                int DB_VERSION_IMPORT = jsonObject.getInt("DB_VERSION");
                if (foundValues == foundValidValues) {
                    return VALID_CONFIGURATION;
                } else if (DB_VERSION_IMPORT > DB_VERSION) {
                    return NEED_UPDATE_CONFIGURATION;
                } else {
                    return INVALID_CONFIGURATION;
                }
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            return INVALID_CONFIGURATION;
        }
        return INVALID_CONFIGURATION;
    }

    public static void shareSettings(Activity activity) {
        new FileSettingsNameDialog(activity, fileName -> {
            try {
                File cacheFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName + ".owl");
                BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile));
                JSONObject object = new JSONObject();

                Field[] fields = OwlConfig.class.getFields();
                for (Field field : fields) {
                    String key = field.getName();
                    try {
                        if ((isBackupAvailable(key) || key.equals("DB_VERSION")) && isNotDeprecatedConfig(key)) {
                            object.put(key, field.get(Object.class));
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                writer.write(object.toString());
                writer.close();

                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", cacheFile);
                } else {
                    uri = Uri.fromFile(cacheFile);
                }
                Intent i = new Intent(Intent.ACTION_SEND);
                if (Build.VERSION.SDK_INT >= 24) {
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, "");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.setClass(activity, LaunchActivity.class);
                activity.startActivity(i);
            } catch (IOException e) {
                FileLog.e(e);
            }
        });
    }

    static File backupFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "owlgram_data.json");
    }

    public static void restoreBackup(MessageObject messageObject) {
        restoreBackup(getSettingFileFromMessage(messageObject), false);
    }

    public static int getDifferenceUI(MessageObject object) {
        return getInternalDifference(getDifferenceBetweenCurrentConfig(object));
    }

    public static int getDifferenceUI() {
        ArrayList<String> listDifference = new ArrayList<>();
        Map<String, ?> mapBackup = getAll();
        for (Map.Entry<?, ?> entry : mapBackup.entrySet()) {
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            listDifference.add(key);
        }
        return getInternalDifference(listDifference);
    }

    private static int getInternalDifference(ArrayList<String> differences) {
        int returnStatus = 0;
        for (int i = 0; i < differences.size(); i++) {
            switch (differences.get(i)) {
                case "fullTime":
                    returnStatus = addWithCheck(returnStatus, NEED_RECREATE_FORMATTERS);
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE_WITH_LAST);
                    break;
                case "showAppBarShadow":
                    returnStatus = addWithCheck(returnStatus, NEED_RECREATE_SHADOW);
                case "roundedNumbers":
                case "useSystemFont":
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE_WITH_LAST);
                    break;
                case "showPencilIcon":
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE);
                    break;
                case "cameraResolution":
                    returnStatus = addWithCheck(returnStatus, NEED_UPDATE_CAMERAX);
                    break;
                case "useSystemEmoji":
                    returnStatus = addWithCheck(returnStatus, NEED_UPDATE_EMOJI);
                    break;
            }
        }
        return returnStatus;
    }

    private static int addWithCheck(int currentTotal, int newValue) {
        if ((currentTotal & newValue) == 0) {
            currentTotal |= newValue;
        }
        return currentTotal;
    }

    public static void doRebuildUIWithDiff(int difference, INavigationLayout parentLayout) {
        NotificationCenter currentAccount = AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter();
        if ((difference & NEED_RECREATE_FORMATTERS) > 0) {
            LocaleController.getInstance().recreateFormatters();
        }
        if ((difference & NEED_RECREATE_SHADOW) > 0) {
            parentLayout.setHeaderShadow(OwlConfig.showAppBarShadow ? parentLayout.getView().getResources().getDrawable(R.drawable.header_shadow).mutate():null);
        }
        if ((difference & NEED_FRAGMENT_REBASE) > 0) {
            parentLayout.rebuildAllFragmentViews(false, false);
        } else if ((difference & NEED_FRAGMENT_REBASE_WITH_LAST) > 0) {
            parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        }
        if ((difference & NEED_UPDATE_CAMERAX) > 0) {
            CameraXUtils.loadSuggestedResolution();
        }
        if ((difference & NEED_UPDATE_EMOJI) > 0) {
            Emoji.reloadEmoji();
            currentAccount.postNotificationName(NotificationCenter.emojiLoaded);
        }
        currentAccount.postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_CHAT);
        currentAccount.postNotificationName(NotificationCenter.mainUserInfoChanged);
        currentAccount.postNotificationName(NotificationCenter.dialogFiltersUpdated);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    public static void restoreBackup(File inputFile, boolean isRestore) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(inputFile);
            JSONObject jsonObject = new JSONObject(new Scanner(stream, "UTF-8")
                    .useDelimiter("\\A")
                    .next());
            if (!isRestore) {
                internalResetSettings();
            }
            for (Iterator<String> data = jsonObject.keys(); data.hasNext(); ) {
                String key = data.next();
                if (isNotDeprecatedConfig(key) && (isRestore || isBackupAvailable(key))) {
                    Object result = jsonObject.get(key);
                    if (result instanceof String) {
                        putValue(key, result);
                    } else if (result instanceof Integer) {
                        putValue(key, result);
                    } else if (result instanceof Boolean) {
                        putValue(key, result);
                    } else if (result instanceof Float) {
                        putValue(key, result);
                    } else if (result instanceof Long) {
                        putValue(key, result);
                    }
                }
            }
            if (!isRestore) {
                configLoaded = false;
                OwlConfig.loadConfig(false);
                MenuOrderManager.reloadConfig();
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    static void executeBackup() {
        new Thread() {
            @Override
            public void run() {
                JSONObject object = new JSONObject();
                Map<String, ?> mapBackup = getAll();
                for (Map.Entry<?, ?> entry : mapBackup.entrySet()) {
                    String key = (String) entry.getKey();
                    if (key == null) {
                        throw new NullPointerException("key == null");
                    }
                    try {
                        object.put(key, entry.getValue());
                    } catch (JSONException e) {
                        FileLog.e(e);
                    }
                }
                try {
                    object.put("DB_VERSION", DB_VERSION);
                } catch (JSONException e) {
                    FileLog.e(e);
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile()));
                    writer.write(object.toString());
                    writer.close();
                } catch (IOException e) {
                    FileLog.e(e);
                }
            }
        }.start();
    }

    public static void resetSettings() {
        internalResetSettings();
        configLoaded = false;
        OwlConfig.loadConfig(false);
        //noinspection ResultOfMethodCallIgnored
        backupFile().delete();
        MenuOrderManager.reloadConfig();
    }

    public static void internalResetSettings() {
        Map<String, ?> mapBackup = getAll();
        for (Map.Entry<?, ?> entry : mapBackup.entrySet()) {
            String key = (String) entry.getKey();
            if (isBackupAvailable(key)) {
                remove(key);
            }
        }
    }
}
