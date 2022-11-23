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
    public static boolean showNameInActionBar;
    public static boolean showPencilIcon;
    public static boolean keepTranslationMarkdown;
    public static boolean uploadSpeedBoost;
    public static boolean hideTimeOnSticker;
    public static boolean showStatusInChat;
    public static boolean showPatpat;
    public static boolean unlockedChupa;
    public static boolean hideAllTab;
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
    public static boolean hideSendAsChannel;

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
                }
                putValue("DB_VERSION", DB_VERSION = BuildVars.BUILD_VERSION);
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
            showNameInActionBar = getBoolean("showNameInActionBar", false);
            showPencilIcon = getBoolean("showPencilIcon", false);
            keepTranslationMarkdown = getBoolean("keepTranslationMarkdown", true);
            hideTimeOnSticker = getBoolean("hideTimeOnSticker", false);
            showStatusInChat = getBoolean("showStatusInChat", false);
            unlockedSecretIcon = getInt("unlockedSecretIcon", 0);
            showPatpat = getBoolean("showPatpat", false);
            unlockedChupa = getBoolean("unlockedChupa", false);
            hideAllTab = getBoolean("hideAllTab", false);
            hideSendAsChannel = getBoolean("hideSendAsChannel", false);

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
    }

    public static void toggleHidePhone() {
        putValue("hidePhoneNumber", hidePhoneNumber ^= true);
    }

    public static void toggleHideContactNumber() {
        putValue("hideContactNumber", hideContactNumber ^= true);
    }

    public static void toggleFullTime() {
        putValue("fullTime", fullTime ^= true);
    }

    public static void toggleRoundedNumbers() {
        putValue("roundedNumbers", roundedNumbers ^= true);
    }

    public static void toggleConfirmCall() {
        putValue("confirmCall", confirmCall ^= true);
    }

    public static void toggleMediaFlipByTap() {
        putValue("mediaFlipByTap", mediaFlipByTap ^= true);
    }

    public static void toggleJumpChannel() {
        putValue("jumpChannel", jumpChannel ^= true);
    }

    public static void toggleHideKeyboard() {
        putValue("hideKeyboard", hideKeyboard ^= true);
    }

    public static void toggleGifAsVideo() {
        putValue("gifAsVideo", gifAsVideo ^= true);
    }

    public static void toggleShowFolderWhenForward() {
        putValue("showFolderWhenForward", showFolderWhenForward ^= true);
    }

    public static void toggleUseRearCamera() {
        putValue("useRearCamera", useRearCamera ^= true);
    }

    public static void toggleSendConfirm() {
        putValue("sendConfirm", sendConfirm ^= true);
    }

    public static void toggleUseSystemFont() {
        putValue("useSystemFont", useSystemFont ^= true);
    }

    public static void toggleUseSystemEmoji() {
        putValue("useSystemEmoji", useSystemEmoji ^= true);
    }

    public static void toggleShowGreetings() {
        putValue("showGreetings", showGreetings ^= true);
    }

    public static void toggleShowTranslate() {
        putValue("showTranslate", showTranslate ^= true);
    }

    public static void toggleShowSaveMessage() {
        putValue("showSaveMessage", showSaveMessage ^= true);
    }

    public static void toggleShowRepeat() {
        putValue("showRepeat", showRepeat ^= true);
    }

    public static void toggleShowMessageDetails() {
        putValue("showMessageDetails", showMessageDetails ^= true);
    }

    public static void toggleShowNoQuoteForwardRow() {
        putValue("showNoQuoteForward", showNoQuoteForward ^= true);
    }

    public static void toggleBetaUpdates() {
        putValue("betaUpdates", betaUpdates ^= true);
    }

    public static void toggleNotifyUpdates() {
        putValue("notifyUpdates", notifyUpdates ^= true);
    }

    public static void toggleAvatarAsDrawerBackground() {
        putValue("avatarAsDrawerBackground", avatarAsDrawerBackground ^= true);
    }

    public static void toggleAvatarBackgroundBlur() {
        putValue("avatarBackgroundBlur", avatarBackgroundBlur ^= true);
    }

    public static void toggleAvatarBackgroundDarken() {
        putValue("avatarBackgroundDarken", avatarBackgroundDarken ^= true);
    }

    public static void toggleShowReportMessage() {
        putValue("showReportMessage", showReportMessage ^= true);
    }

    public static void toggleBetterAudioQuality() {
        putValue("betterAudioQuality", betterAudioQuality ^= true);
    }

    public static void toggleShowGradientColor() {
        putValue("showGradientColor", showGradientColor ^= true);
    }

    public static void toggleShowAvatarImage() {
        putValue("showAvatarImage", showAvatarImage ^= true);
    }

    public static void toggleOwlEasterSound() {
        putValue("owlEasterSound", owlEasterSound ^= true);
    }

    public static void togglePacmanForced() {
        putValue("pacmanForced", pacmanForced ^= true);
    }

    public static void toggleSmartButtons() {
        putValue("smartButtons", smartButtons ^= true);
    }

    public static void toggleAppBarShadow() {
        putValue("disableAppBarShadow", disableAppBarShadow ^= true);
    }

    public static void toggleAccentColor() {
        putValue("accentAsNotificationColor", accentAsNotificationColor ^= true);
    }

    public static void toggleShowDeleteDownloadedFile() {
        putValue("showDeleteDownloadedFile", showDeleteDownloadedFile ^= true);
    }

    public static void toggleShowCopyPhoto() {
        putValue("showCopyPhoto", showCopyPhoto ^= true);
    }

    public static void toggleShowNameInActionBar() {
        putValue("showNameInActionBar", showNameInActionBar ^= true);
    }

    public static void toggleShowSantaHat() {
        putValue("showSantaHat", showSantaHat ^= true);
    }

    public static void toggleShowSnowFalling() {
        putValue("showSnowFalling", showSnowFalling ^= true);
    }

    public static void toggleCameraXOptimizedMode() {
        putValue("useCameraXOptimizedMode", useCameraXOptimizedMode ^= true);
    }

    public static void toggleDisableProximityEvents() {
        putValue("disableProximityEvents", disableProximityEvents ^= true);
    }

    public static void toggleVoicesAgc() {
        putValue("voicesAgc", voicesAgc ^= true);
    }

    public static void toggleTurnSoundOnVDKey() {
        putValue("turnSoundOnVDKey", turnSoundOnVDKey ^= true);
    }

    public static void toggleOpenArchiveOnPull() {
        putValue("openArchiveOnPull", openArchiveOnPull ^= true);
    }

    public static void toggleSlidingChatTitle() {
        putValue("slidingChatTitle", slidingChatTitle ^= true);
    }

    public static void toggleConfirmStickersGIFs() {
        putValue("confirmStickersGIFs", confirmStickersGIFs ^= true);
    }

    public static void toggleShowIDAndDC() {
        putValue("showIDAndDC", showIDAndDC ^= true);
    }

    public static void toggleSearchIconInActionBar() {
        putValue("searchIconInActionBar", searchIconInActionBar ^= true);
    }

    public static void toggleAutoTranslate() {
        putValue("autoTranslate", autoTranslate ^= true);
    }

    public static void toggleShowPencilIcon() {
        putValue("showPencilIcon", showPencilIcon ^= true);
    }

    public static void toggleKeepTranslationMarkdown() {
        putValue("keepTranslationMarkdown", keepTranslationMarkdown ^= true);
    }

    public static void toggleUploadSpeedBoost() {
        putValue("uploadSpeedBoost", uploadSpeedBoost ^= true);
    }

    public static void toggleHideTimeOnSticker() {
        putValue("hideTimeOnSticker", hideTimeOnSticker ^= true);
    }

    public static void toggleShowStatusInChat() {
        putValue("showStatusInChat", showStatusInChat ^= true);
    }

    public static void toggleShowPatpat() {
        putValue("showPatpat", showPatpat ^= true);
    }

    public static void toggleHideAllTab() {
        putValue("hideAllTab", hideAllTab ^= true);
    }

    public static void toggleHideSendAsChannel() {
        putValue("hideSendAsChannel", hideSendAsChannel ^= true);
    }

    public static void unlockChupa() {
        putValue("unlockedChupa", unlockedChupa = true);
    }

    public static void setUnlockedSecretIcon(int value) {
        putValue("unlockedSecretIcon", unlockedSecretIcon = value);
    }

    public static void setXiaomiBlockedInstaller() {
        putValue("xiaomiBlockedInstaller", xiaomiBlockedInstaller = true);
    }

    public static void setVerifyLinkTip(boolean shown) {
        putValue("verifyLinkTip", verifyLinkTip = shown);
    }

    public static void setTranslationProvider(int provider) {
        putValue("translationProvider", translationProvider = provider);
    }

    public static void setTranslationTarget(String target) {
        putValue("translationTarget", translationTarget = target);
    }

    public static void setTranslationKeyboardTarget(String target) {
        putValue("translationKeyboardTarget", translationKeyboardTarget = target);
    }

    public static void setDeepLFormality(int formality) {
        putValue("deepLFormality", deepLFormality = formality);
    }

    public static void setTranslatorStyle(int style) {
        putValue("translatorStyle", translatorStyle = style);
    }

    public static void setTabMode(int mode) {
        putValue("tabMode", tabMode = mode);
    }

    public static void setStickerSize(int size) {
        putValue("stickerSizeStack", stickerSizeStack = size);
    }

    public static void setDcStyleType(int type) {
        putValue("dcStyleType", dcStyleType = type);
    }

    public static void setIdType(int type) {
        putValue("idType", idType = type);
    }

    public static String currentNotificationVersion() {
        return BuildVars.BUILD_VERSION_STRING + "_" + BuildVars.BUILD_VERSION;
    }

    public static void updateCurrentVersion() {
        putValue("oldBuildVersion", oldBuildVersion = currentNotificationVersion());
    }

    public static void saveLastUpdateCheck() {
        saveLastUpdateCheck(false);
    }

    public static void saveLastUpdateCheck(boolean isReset) {
        putValue("lastUpdateCheck", lastUpdateCheck = isReset ? 0 : new Date().getTime());
    }

    public static void saveUpdateStatus(int status) {
        putValue("lastUpdateStatus", lastUpdateStatus = status);
    }

    public static void saveBlurIntensity(int intensity) {
        putValue("blurIntensity", blurIntensity = intensity);
    }

    public static void remindUpdate(int version) {
        putValue("remindedUpdate", remindedUpdate = version);
        saveUpdateStatus(0);
    }

    public static void saveOldVersion(int version) {
        putValue("oldDownloadedVersion", oldDownloadedVersion = version);
    }

    public static void saveButtonStyle(int type) {
        putValue("buttonStyleType", buttonStyleType = type);
    }

    public static void saveEventType(int type) {
        putValue("eventType", eventType = type);
    }

    public static void saveCameraType(int type) {
        putValue("cameraType", cameraType = type);
    }

    public static void saveCameraXFps(int fps) {
        putValue("cameraXFps", cameraXFps = fps);
    }

    public static void setMaxRecentStickers(int size) {
        putValue("maxRecentStickers", maxRecentStickers = size);
    }

    public static void setUpdateData(String data) {
        putValue("updateData", updateData = data);
    }

    public static void setDrawerItems(String data) {
        putValue("drawerItems", drawerItems = data);
    }

    public static void setLanguagePackVersioning(String data) {
        putValue("languagePackVersioning", languagePackVersioning = data);
    }

    public static void setDoNotTranslateLanguages(String data) {
        putValue("doNotTranslateLanguages", doNotTranslateLanguages = data);
    }

    public static void setDownloadSpeedBoost(int boost) {
        putValue("downloadSpeedBoost", downloadSpeedBoost = boost);
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
        putValue("devOptEnabled", devOptEnabled ^= true);
        loadConfig(configLoaded = false);
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