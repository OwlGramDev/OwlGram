package it.owlgram.android;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;

import java.util.Calendar;
import java.util.Date;

import it.owlgram.android.camera.CameraXUtilities;
import it.owlgram.android.helpers.MonetIconsHelper;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;

public class OwlConfig extends SettingsManager {
    public static final int TAB_TYPE_TEXT = 0;
    public static final int TAB_TYPE_MIX = 1;
    public static final int TAB_TYPE_ICON = 2;

    public static final int DOWNLOAD_BOOST_DEFAULT = 0;
    public static final int DOWNLOAD_BOOST_FAST = 1;
    public static final int DOWNLOAD_BOOST_EXTREME = 2;

    private static final Object sync = new Object();
    public static boolean hidePhoneNumber;
    public static boolean hideContactNumber;
    public static boolean fullTime;
    public static boolean roundedNumbers;
    public static boolean confirmCall;
    public static boolean mediaFlipByTap;
    public static boolean jumpChannel;
    public static boolean hideKeyboard;
    public static boolean gifAsVideo;
    public static boolean showFolderWhenForward;
    public static boolean useRearCamera;
    public static boolean sendConfirm;
    public static boolean useSystemFont;
    public static boolean useSystemEmoji;
    public static boolean showGreetings;
    public static boolean showTranslate;
    public static boolean showSaveMessage;
    public static boolean showRepeat;
    public static boolean showNoQuoteForward;
    public static boolean showCopyPhoto;
    public static boolean showMessageDetails;
    public static boolean betaUpdates;
    public static boolean notifyUpdates;
    public static boolean avatarBackgroundDarken;
    public static boolean avatarBackgroundBlur;
    public static boolean avatarAsDrawerBackground;
    public static boolean betterAudioQuality;
    public static boolean showReportMessage;
    public static boolean showGradientColor;
    public static boolean showAvatarImage;
    public static boolean owlEasterSound;
    public static boolean pacmanForced;
    public static boolean smartButtons;
    public static boolean disableAppBarShadow;
    public static boolean accentAsNotificationColor;
    public static boolean showDeleteDownloadedFile;
    public static boolean isChineseUser = false;
    public static boolean showSantaHat;
    public static boolean showSnowFalling;
    public static boolean useCameraXOptimizedMode;
    public static boolean disableProximityEvents;
    public static boolean devOptEnabled;
    public static boolean verifyLinkTip;
    public static boolean voicesAgc;
    public static boolean turnSoundOnVDKey;
    public static boolean openArchiveOnPull;
    public static boolean slidingChatTitle;
    public static boolean confirmStickersGIFs;
    public static boolean showIDAndDC;
    public static boolean xiaomiBlockedInstaller;
    public static boolean searchIconInActionBar;
    public static boolean autoTranslate;
    public static boolean showPencilIcon;
    public static boolean keepTranslationMarkdown;
    public static boolean uploadSpeedBoost;
    public static boolean hideTimeOnSticker;
    public static boolean showStatusInChat;
    public static boolean showPatpat;
    public static boolean unlockedChupa;
    public static boolean hideAllTab;
    public static boolean hideSendAsChannel;
    public static String translationTarget = "app";
    public static String translationKeyboardTarget = "app";
    public static String updateData;
    public static String drawerItems;
    public static String oldBuildVersion = null;
    public static String languagePackVersioning;
    public static String doNotTranslateLanguages;
    public static int deepLFormality;
    public static int translationProvider;
    public static int lastUpdateStatus = 0;
    public static int tabMode = 0;
    public static int remindedUpdate = 0;
    public static int oldDownloadedVersion = 0;
    public static int blurIntensity = 0;
    public static int eventType = 0;
    public static int buttonStyleType = 0;
    public static int translatorStyle = 0;
    public static int cameraType;
    public static int cameraXFps;
    public static int maxRecentStickers;
    public static int stickerSizeStack = 0;
    public static int dcStyleType;
    public static int idType;
    public static long lastUpdateCheck = 0;
    public static int downloadSpeedBoost;
    public static int unlockedSecretIcon;
    public static int showInActionBar;

    static {
        loadConfig(true);
    }

    public static void loadConfig(boolean firstLoad) {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            //VERSION_CHECK
            if (firstLoad) {
                boolean backupFileExist = backupFile().exists();
                DB_VERSION = getInt("DB_VERSION", 0);
                if (DB_VERSION < BuildVars.BUILD_VERSION && backupFileExist) {
                    restoreBackup(backupFile(), true);
                    DB_VERSION = BuildVars.BUILD_VERSION;
                    putValue("DB_VERSION", DB_VERSION);
                }
                registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> executeBackup());
            }
            isChineseUser = ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isChineseUser);
            hidePhoneNumber = getBoolean("hidePhoneNumber", true);
            hideContactNumber = getBoolean("hideContactNumber", true);
            fullTime = getBoolean("fullTime", false);
            roundedNumbers = getBoolean("roundedNumbers", true);
            confirmCall = getBoolean("confirmCall", true);
            mediaFlipByTap = getBoolean("mediaFlipByTap", true);
            jumpChannel = getBoolean("jumpChannel", true);
            hideKeyboard = getBoolean("hideKeyboard", false);
            gifAsVideo = getBoolean("gifAsVideo", false);
            showFolderWhenForward = getBoolean("showFolderWhenForward", true);
            useRearCamera = getBoolean("useRearCamera", false);
            sendConfirm = getBoolean("sendConfirm", false);
            useSystemFont = getBoolean("useSystemFont", false);
            useSystemEmoji = getBoolean("useSystemEmoji", false);
            showGreetings = getBoolean("showGreetings", true);
            showTranslate = getBoolean("showTranslate", false);
            showSaveMessage = getBoolean("showSaveMessage", false);
            showRepeat = getBoolean("showRepeat", false);
            showNoQuoteForward = getBoolean("showNoQuoteForward", false);
            showMessageDetails = getBoolean("showMessageDetails", false);
            betaUpdates = getBoolean("betaUpdates", false);
            notifyUpdates = getBoolean("notifyUpdates", true);
            avatarBackgroundDarken = getBoolean("avatarBackgroundDarken", false);
            avatarBackgroundBlur = getBoolean("avatarBackgroundBlur", false);
            avatarAsDrawerBackground = getBoolean("avatarAsDrawerBackground", false);
            showReportMessage = getBoolean("showReportMessage", true);
            showGradientColor = getBoolean("showGradientColor", false);
            showAvatarImage = getBoolean("showAvatarImage", true);
            owlEasterSound = getBoolean("owlEasterSound", true);
            pacmanForced = getBoolean("pacmanForced", false);
            smartButtons = getBoolean("smartButtons", false);
            disableAppBarShadow = getBoolean("disableAppBarShadow", false);
            accentAsNotificationColor = getBoolean("accentAsNotificationColor", false);
            showDeleteDownloadedFile = getBoolean("showDeleteDownloadedFile", false);
            lastUpdateCheck = getLong("lastUpdateCheck", 0);
            lastUpdateStatus = getInt("lastUpdateStatus", 0);
            remindedUpdate = getInt("remindedUpdate", 0);
            translationTarget = getString("translationTarget", "app");
            translationKeyboardTarget = getString("translationKeyboardTarget", "app");
            updateData = getString("updateData", "");
            drawerItems = getString("drawerItems", "[]");
            oldDownloadedVersion = getInt("oldDownloadedVersion", 0);
            eventType = getInt("eventType", 0);
            buttonStyleType = getInt("buttonStyleType", 0);
            tabMode = getInt("tabMode", 1);
            translatorStyle = getInt("translatorStyle", BaseTranslator.INLINE_STYLE);
            blurIntensity = getInt("blurIntensity", 75);
            oldBuildVersion = getString("oldBuildVersion", null);
            stickerSizeStack = getInt("stickerSizeStack", 14);
            deepLFormality = getInt("deepLFormality", DeepLTranslator.FORMALITY_DEFAULT);
            translationProvider = getInt("translationProvider", Translator.PROVIDER_GOOGLE);
            showSantaHat = getBoolean("showSantaHat", true);
            showSnowFalling = getBoolean("showSnowFalling", true);
            cameraType = getInt("cameraType", CameraXUtilities.getDefault());
            cameraXFps = getInt("cameraXFps", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH ? 60 : 30);
            useCameraXOptimizedMode = getBoolean("useCameraXOptimizedMode", SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_HIGH);
            disableProximityEvents = getBoolean("disableProximityEvents", false);
            verifyLinkTip = getBoolean("verifyLinkTip", false);
            languagePackVersioning = getString("languagePackVersioning", "{}");
            xiaomiBlockedInstaller = getBoolean("xiaomiBlockedInstaller", false);
            voicesAgc = getBoolean("voicesAgc", false);
            turnSoundOnVDKey = getBoolean("turnSoundOnVDKey", true);
            openArchiveOnPull = getBoolean("openArchiveOnPull", false);
            slidingChatTitle = getBoolean("slidingChatTitle", false);
            confirmStickersGIFs = getBoolean("confirmStickersGIFs", false);
            showIDAndDC = getBoolean("showIDAndDC", false);
            doNotTranslateLanguages = getString("doNotTranslateLanguages", "[\"app\"]");
            dcStyleType = getInt("dcStyleType", 0);
            idType = getInt("idType", 0);
            searchIconInActionBar = getBoolean("searchIconInActionBar", false);
            autoTranslate = getBoolean("autoTranslate", false);
            showCopyPhoto = getBoolean("showCopyPhoto", false);
            showPencilIcon = getBoolean("showPencilIcon", false);
            keepTranslationMarkdown = getBoolean("keepTranslationMarkdown", true);
            hideTimeOnSticker = getBoolean("hideTimeOnSticker", false);
            showStatusInChat = getBoolean("showStatusInChat", false);
            unlockedSecretIcon = getInt("unlockedSecretIcon", 0);
            showPatpat = getBoolean("showPatpat", false);
            unlockedChupa = getBoolean("unlockedChupa", false);
            hideAllTab = getBoolean("hideAllTab", false);
            hideSendAsChannel = getBoolean("hideSendAsChannel", false);
            showInActionBar = getInt("showInActionBar", 0);

            //EXPERIMENTAL OPTIONS
            devOptEnabled = getBoolean("devOptEnabled", false);

            String dS = devOptEnabled ? "" : "_disabled";
            maxRecentStickers = getInt("maxRecentStickers" + dS, 20);
            betterAudioQuality = getBoolean("betterAudioQuality" + dS, false);
            downloadSpeedBoost = getInt("downloadSpeedBoost" + dS, 0);
            uploadSpeedBoost = getBoolean("uploadSpeedBoost" + dS, false);
            configLoaded = true;
            migrate();
        }
    }

    private static void migrate() {
        if (translationProvider == Translator.PROVIDER_NIU) {
            setTranslationProvider(Translator.PROVIDER_GOOGLE);
        }
        if (getBoolean("showNameInActionBar", false)) {
            setShowInActionBar(1);
            remove("showNameInActionBar");
        }
    }

    public static void toggleHidePhone() {
        hidePhoneNumber = !hidePhoneNumber;
        putValue("hidePhoneNumber", hidePhoneNumber);
    }

    public static void toggleHideContactNumber() {
        hideContactNumber = !hideContactNumber;
        putValue("hideContactNumber", hideContactNumber);
    }

    public static void toggleFullTime() {
        fullTime = !fullTime;
        putValue("fullTime", fullTime);
    }

    public static void toggleRoundedNumbers() {
        roundedNumbers = !roundedNumbers;
        putValue("roundedNumbers", roundedNumbers);
    }

    public static void toggleConfirmCall() {
        confirmCall = !confirmCall;
        putValue("confirmCall", confirmCall);
    }

    public static void toggleMediaFlipByTap() {
        mediaFlipByTap = !mediaFlipByTap;
        putValue("mediaFlipByTap", mediaFlipByTap);
    }

    public static void toggleJumpChannel() {
        jumpChannel = !jumpChannel;
        putValue("jumpChannel", jumpChannel);
    }

    public static void toggleHideKeyboard() {
        hideKeyboard = !hideKeyboard;
        putValue("hideKeyboard", hideKeyboard);
    }

    public static void toggleGifAsVideo() {
        gifAsVideo = !gifAsVideo;
        putValue("gifAsVideo", gifAsVideo);
    }

    public static void toggleShowFolderWhenForward() {
        showFolderWhenForward = !showFolderWhenForward;
        putValue("showFolderWhenForward", showFolderWhenForward);
    }

    public static void toggleUseRearCamera() {
        useRearCamera = !useRearCamera;
        putValue("useRearCamera", useRearCamera);
    }

    public static void toggleSendConfirm() {
        sendConfirm = !sendConfirm;
        putValue("sendConfirm", sendConfirm);
    }

    public static void toggleUseSystemFont() {
        useSystemFont = !useSystemFont;
        putValue("useSystemFont", useSystemFont);
    }

    public static void toggleUseSystemEmoji() {
        useSystemEmoji = !useSystemEmoji;
        putValue("useSystemEmoji", useSystemEmoji);
    }

    public static void toggleShowGreetings() {
        showGreetings = !showGreetings;
        putValue("showGreetings", showGreetings);
    }

    public static void toggleShowTranslate() {
        showTranslate = !showTranslate;
        putValue("showTranslate", showTranslate);
    }

    public static void toggleShowSaveMessage() {
        showSaveMessage = !showSaveMessage;
        putValue("showSaveMessage", showSaveMessage);
    }

    public static void toggleShowRepeat() {
        showRepeat = !showRepeat;
        putValue("showRepeat", showRepeat);
    }

    public static void toggleShowMessageDetails() {
        showMessageDetails = !showMessageDetails;
        putValue("showMessageDetails", showMessageDetails);
    }

    public static void toggleShowNoQuoteForwardRow() {
        showNoQuoteForward = !showNoQuoteForward;
        putValue("showNoQuoteForward", showNoQuoteForward);
    }

    public static void toggleBetaUpdates() {
        betaUpdates = !betaUpdates;
        putValue("betaUpdates", betaUpdates);
    }

    public static void toggleNotifyUpdates() {
        notifyUpdates = !notifyUpdates;
        putValue("notifyUpdates", notifyUpdates);
    }

    public static void toggleAvatarAsDrawerBackground() {
        avatarAsDrawerBackground = !avatarAsDrawerBackground;
        putValue("avatarAsDrawerBackground", avatarAsDrawerBackground);
    }

    public static void toggleAvatarBackgroundBlur() {
        avatarBackgroundBlur = !avatarBackgroundBlur;
        putValue("avatarBackgroundBlur", avatarBackgroundBlur);
    }

    public static void toggleAvatarBackgroundDarken() {
        avatarBackgroundDarken = !avatarBackgroundDarken;
        putValue("avatarBackgroundDarken", avatarBackgroundDarken);
    }

    public static void toggleShowReportMessage() {
        showReportMessage = !showReportMessage;
        putValue("showReportMessage", showReportMessage);
    }

    public static void toggleBetterAudioQuality() {
        betterAudioQuality = !betterAudioQuality;
        putValue("betterAudioQuality", betterAudioQuality);
    }

    public static void toggleShowGradientColor() {
        showGradientColor = !showGradientColor;
        putValue("showGradientColor", showGradientColor);
    }

    public static void toggleShowAvatarImage() {
        showAvatarImage = !showAvatarImage;
        putValue("showAvatarImage", showAvatarImage);
    }

    public static void toggleOwlEasterSound() {
        owlEasterSound = !owlEasterSound;
        putValue("owlEasterSound", owlEasterSound);
    }

    public static void togglePacmanForced() {
        pacmanForced = !pacmanForced;
        putValue("pacmanForced", pacmanForced);
    }

    public static void toggleSmartButtons() {
        smartButtons = !smartButtons;
        putValue("smartButtons", smartButtons);
    }

    public static void toggleAppBarShadow() {
        disableAppBarShadow = !disableAppBarShadow;
        putValue("disableAppBarShadow", disableAppBarShadow);
    }

    public static void toggleAccentColor() {
        accentAsNotificationColor = !accentAsNotificationColor;
        putValue("accentAsNotificationColor", accentAsNotificationColor);
    }

    public static void toggleShowDeleteDownloadedFile() {
        showDeleteDownloadedFile = !showDeleteDownloadedFile;
        putValue("showDeleteDownloadedFile", showDeleteDownloadedFile);
    }

    public static void toggleShowCopyPhoto() {
        showCopyPhoto = !showCopyPhoto;
        putValue("showCopyPhoto", showCopyPhoto);
    }

    public static void toggleShowSantaHat() {
        showSantaHat = !showSantaHat;
        putValue("showSantaHat", showSantaHat);
    }

    public static void toggleShowSnowFalling() {
        showSnowFalling = !showSnowFalling;
        putValue("showSnowFalling", showSnowFalling);
    }

    public static void toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode;
        putValue("useCameraXOptimizedMode", useCameraXOptimizedMode);
    }

    public static void toggleDisableProximityEvents() {
        disableProximityEvents = !disableProximityEvents;
        putValue("disableProximityEvents", disableProximityEvents);
    }

    public static void toggleVoicesAgc() {
        voicesAgc = !voicesAgc;
        putValue("voicesAgc", voicesAgc);
    }

    public static void toggleTurnSoundOnVDKey() {
        turnSoundOnVDKey = !turnSoundOnVDKey;
        putValue("turnSoundOnVDKey", turnSoundOnVDKey);
    }

    public static void toggleOpenArchiveOnPull() {
        openArchiveOnPull = !openArchiveOnPull;
        putValue("openArchiveOnPull", openArchiveOnPull);
    }

    public static void toggleSlidingChatTitle() {
        slidingChatTitle = !slidingChatTitle;
        putValue("slidingChatTitle", slidingChatTitle);
    }

    public static void toggleConfirmStickersGIFs() {
        confirmStickersGIFs = !confirmStickersGIFs;
        putValue("confirmStickersGIFs", confirmStickersGIFs);
    }

    public static void toggleShowIDAndDC() {
        showIDAndDC = !showIDAndDC;
        putValue("showIDAndDC", showIDAndDC);
    }

    public static void toggleSearchIconInActionBar() {
        searchIconInActionBar = !searchIconInActionBar;
        putValue("searchIconInActionBar", searchIconInActionBar);
    }

    public static void toggleShowPencilIcon() {
        showPencilIcon = !showPencilIcon;
        putValue("showPencilIcon", showPencilIcon);
    }

    public static void toggleKeepTranslationMarkdown() {
        keepTranslationMarkdown = !keepTranslationMarkdown;
        putValue("keepTranslationMarkdown", keepTranslationMarkdown);
    }

    public static void toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost;
        putValue("uploadSpeedBoost", uploadSpeedBoost);
    }

    public static void toggleHideTimeOnSticker() {
        hideTimeOnSticker = !hideTimeOnSticker;
        putValue("hideTimeOnSticker", hideTimeOnSticker);
    }

    public static void toggleShowStatusInChat() {
        showStatusInChat = !showStatusInChat;
        putValue("showStatusInChat", showStatusInChat);
    }

    public static void toggleShowPatpat() {
        showPatpat = !showPatpat;
        putValue("showPatpat", showPatpat);
    }

    public static void toggleHideAllTab() {
        hideAllTab = !hideAllTab;
        putValue("hideAllTab", hideAllTab);
    }

    public static void unlockChupa() {
        unlockedChupa = true;
        putValue("unlockedChupa", true);
    }

    public static void setUnlockedSecretIcon(int value) {
        unlockedSecretIcon = value;
        putValue("unlockedSecretIcon", unlockedSecretIcon);
    }

    public static void setXiaomiBlockedInstaller() {
        xiaomiBlockedInstaller = true;
        putValue("xiaomiBlockedInstaller", true);
    }

    public static void setVerifyLinkTip(boolean shown) {
        verifyLinkTip = shown;
        putValue("verifyLinkTip", verifyLinkTip);
    }

    public static void setAutoTranslate(boolean enabled) {
        putValue("autoTranslate", autoTranslate = enabled);
    }

    public static void setTranslationProvider(int provider) {
        translationProvider = provider;
        putValue("translationProvider", translationProvider);
    }

    public static void setTranslationTarget(String target) {
        translationTarget = target;
        putValue("translationTarget", translationTarget);
    }

    public static void setTranslationKeyboardTarget(String target) {
        translationKeyboardTarget = target;
        putValue("translationKeyboardTarget", translationKeyboardTarget);
    }

    public static void setDeepLFormality(int formality) {
        deepLFormality = formality;
        putValue("deepLFormality", deepLFormality);
    }

    public static void setTranslatorStyle(int style) {
        translatorStyle = style;
        putValue("translatorStyle", translatorStyle);
    }

    public static void setTabMode(int mode) {
        tabMode = mode;
        putValue("tabMode", tabMode);
    }

    public static void setStickerSize(int size) {
        stickerSizeStack = size;
        putValue("stickerSizeStack", stickerSizeStack);
    }

    public static void setDcStyleType(int type) {
        dcStyleType = type;
        putValue("dcStyleType", dcStyleType);
    }

    public static void setIdType(int type) {
        idType = type;
        putValue("idType", idType);
    }

    public static String currentNotificationVersion() {
        return BuildVars.BUILD_VERSION_STRING + "_" + BuildVars.BUILD_VERSION;
    }

    public static void updateCurrentVersion() {
        oldBuildVersion = currentNotificationVersion();
        putValue("oldBuildVersion", oldBuildVersion);
    }

    public static void saveLastUpdateCheck() {
        saveLastUpdateCheck(false);
    }

    public static void saveLastUpdateCheck(boolean isReset) {
        lastUpdateCheck = isReset ? 0 : new Date().getTime();
        putValue("lastUpdateCheck", lastUpdateCheck);
    }

    public static void saveUpdateStatus(int status) {
        lastUpdateStatus = status;
        putValue("lastUpdateStatus", lastUpdateStatus);
    }

    public static void saveBlurIntensity(int intensity) {
        blurIntensity = intensity;
        putValue("blurIntensity", blurIntensity);
    }

    public static void remindUpdate(int version) {
        remindedUpdate = version;
        putValue("remindedUpdate", remindedUpdate);
        saveUpdateStatus(0);
    }

    public static void saveOldVersion(int version) {
        oldDownloadedVersion = version;
        putValue("oldDownloadedVersion", oldDownloadedVersion);
    }

    public static void saveButtonStyle(int type) {
        buttonStyleType = type;
        putValue("buttonStyleType", buttonStyleType);
    }

    public static void saveEventType(int type) {
        eventType = type;
        putValue("eventType", eventType);
    }

    public static void saveCameraType(int type) {
        cameraType = type;
        putValue("cameraType", cameraType);
    }

    public static void saveCameraXFps(int fps) {
        cameraXFps = fps;
        putValue("cameraXFps", cameraXFps);
    }

    public static void setMaxRecentStickers(int size) {
        maxRecentStickers = size;
        putValue("maxRecentStickers", maxRecentStickers);
    }

    public static void setShowInActionBar(int type) {
        putValue("showInActionBar", showInActionBar = type);
    }

    public static void setUpdateData(String data) {
        updateData = data;
        putValue("updateData", updateData);
    }

    public static void setDrawerItems(String data) {
        drawerItems = data;
        putValue("drawerItems", drawerItems);
    }

    public static void setLanguagePackVersioning(String data) {
        languagePackVersioning = data;
        putValue("languagePackVersioning", languagePackVersioning);
    }

    public static void setDoNotTranslateLanguages(String data) {
        doNotTranslateLanguages = data;
        putValue("doNotTranslateLanguages", doNotTranslateLanguages);
    }

    public static void setDownloadSpeedBoost(int boost) {
        downloadSpeedBoost = boost;
        putValue("downloadSpeedBoost", downloadSpeedBoost);
    }

    public static int getNotificationColor() {
        if (accentAsNotificationColor) {
            int color = 0;
            if (Theme.getActiveTheme().hasAccentColors()) {
                color = Theme.getActiveTheme().getAccentColor(Theme.getActiveTheme().currentAccentId);
            }
            if (color == 0) {
                color = Theme.getColor(Theme.key_actionBarDefault) | 0xff000000;
            }
            float brightness = AndroidUtilities.computePerceivedBrightness(color);
            if (brightness >= 0.721f || brightness <= 0.279f) {
                color = Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader) | 0xff000000;
            }
            return color;
        } else {
            return 0xff11acfa;
        }
    }

    public static void toggleDevOpt() {
        devOptEnabled = !devOptEnabled;
        putValue("devOptEnabled", devOptEnabled);
        configLoaded = false;
        loadConfig(false);
    }

    public static boolean isDevOptEnabled() {
        return devOptEnabled || betterAudioQuality || MonetIconsHelper.isSelectedMonet() || maxRecentStickers != 20;
    }

    public static boolean canShowFireworks() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int monthOfYear = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return monthOfYear == 1 && dayOfMonth == 1;
    }
}