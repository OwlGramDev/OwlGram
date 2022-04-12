package it.owlgram.android;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import it.owlgram.android.camera.CameraXUtilities;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;

public class OwlConfig extends SettingsManager {
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
    public static boolean scrollableChatPreview;
    public static boolean smartButtons;
    public static boolean disableAppBarShadow;
    public static boolean accentAsNotificationColor;
    public static boolean showDeleteDownloadedFile;
    public static boolean isChineseUser = false;
    public static boolean hideAllTab;
    public static boolean showSantaHat;
    public static boolean showSnowFalling;
    public static boolean useCameraXOptimizedMode;
    public static boolean disableProximityEvents;
    public static boolean swipeToPiP;
    public static boolean unlimitedFavoriteStickers;
    public static boolean unlimitedPinnedDialogs;
    public static boolean devOptEnabled;
    public static boolean verifyLinkTip;
    public static boolean increaseAudioMessages;
    public static boolean voicesAgc;
    public static boolean turnSoundOnVDKey;
    public static boolean openArchiveOnPull;
    public static boolean slidingChatTitle;
    public static boolean confirmStickersGIFs;
    public static boolean showIDAndDC;
    public static boolean xiaomiBlockedInstaller;
    public static boolean searchIconInActionBar;
    public static String translationTarget = "app";
    public static String translationKeyboardTarget = "app";
    public static String updateData;
    public static String drawerItems;
    public static String oldBuildVersion = null;
    public static String languagePackVersioning;
    public static String doNotTranslateLanguages;
    public static int deepLFormality = DeepLTranslator.FORMALITY_DEFAULT;
    public static int translationProvider = Translator.PROVIDER_GOOGLE;
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

    static {
        loadConfig(true);
    }

    public static void loadConfig(boolean firstLoad) {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
            //VERSION_CHECK
            if (firstLoad) {
                boolean backupFileExist = backupFile().exists();
                DB_VERSION = preferences.getInt("DB_VERSION", 0);
                if ((DB_VERSION < BuildVars.BUILD_VERSION || !backupFileExist) && preferences.getAll().size() > 0) {
                    DB_VERSION = BuildVars.BUILD_VERSION;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("DB_VERSION", DB_VERSION);
                    editor.apply();
                    if (preferences.getAll().size() != 0 || backupFileExist) {
                        if (!backupFileExist) {
                            executeBackup();
                        } else {
                            restoreBackup(backupFile(), true);
                        }
                    }
                }
                preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> executeBackup());
            }
            isChineseUser = ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isChineseUser);
            hidePhoneNumber = preferences.getBoolean("hidePhoneNumber", true);
            hideContactNumber = preferences.getBoolean("hideContactNumber", true);
            fullTime = preferences.getBoolean("fullTime", false);
            roundedNumbers = preferences.getBoolean("roundedNumbers", false);
            confirmCall = preferences.getBoolean("confirmCall", true);
            mediaFlipByTap = preferences.getBoolean("mediaFlipByTap", true);
            jumpChannel = preferences.getBoolean("jumpChannel", true);
            hideKeyboard = preferences.getBoolean("hideKeyboard", false);
            gifAsVideo = preferences.getBoolean("gifAsVideo", true);
            showFolderWhenForward = preferences.getBoolean("showFolderWhenForward", true);
            useRearCamera = preferences.getBoolean("useRearCamera", false);
            sendConfirm = preferences.getBoolean("sendConfirm", false);
            useSystemFont = preferences.getBoolean("useSystemFont", false);
            useSystemEmoji = preferences.getBoolean("useSystemEmoji", false);
            showGreetings = preferences.getBoolean("showGreetings", true);
            showTranslate = preferences.getBoolean("showTranslate", true);
            showSaveMessage = preferences.getBoolean("showSaveMessage", true);
            showRepeat = preferences.getBoolean("showRepeat", false);
            showNoQuoteForward = preferences.getBoolean("showNoQuoteForward", false);
            showMessageDetails = preferences.getBoolean("showMessageDetails", false);
            betaUpdates = preferences.getBoolean("betaUpdates", false);
            notifyUpdates = preferences.getBoolean("notifyUpdates", true);
            avatarBackgroundDarken = preferences.getBoolean("avatarBackgroundDarken", false);
            avatarBackgroundBlur = preferences.getBoolean("avatarBackgroundBlur", false);
            avatarAsDrawerBackground = preferences.getBoolean("avatarAsDrawerBackground", false);
            showReportMessage = preferences.getBoolean("showReportMessage", true);
            showGradientColor = preferences.getBoolean("showGradientColor", false);
            showAvatarImage = preferences.getBoolean("showAvatarImage", true);
            owlEasterSound = preferences.getBoolean("owlEasterSound", true);
            pacmanForced = preferences.getBoolean("pacmanForced", false);
            scrollableChatPreview = preferences.getBoolean("scrollableChatPreview", true);
            smartButtons = preferences.getBoolean("smartButtons", false);
            disableAppBarShadow = preferences.getBoolean("disableAppBarShadow", false);
            accentAsNotificationColor = preferences.getBoolean("accentAsNotificationColor", false);
            showDeleteDownloadedFile = preferences.getBoolean("showDeleteDownloadedFile", false);
            hideAllTab = preferences.getBoolean("hideAllTab", false);
            lastUpdateCheck = preferences.getLong("lastUpdateCheck", 0);
            lastUpdateStatus = preferences.getInt("lastUpdateStatus", 0);
            remindedUpdate = preferences.getInt("remindedUpdate", 0);
            translationTarget = preferences.getString("translationTarget", "app");
            translationKeyboardTarget = preferences.getString("translationKeyboardTarget", "app");
            updateData = preferences.getString("updateData", "");
            drawerItems = preferences.getString("drawerItems", "[]");
            oldDownloadedVersion = preferences.getInt("oldDownloadedVersion", 0);
            eventType = preferences.getInt("eventType", 0);
            buttonStyleType = preferences.getInt("buttonStyleType", 0);
            tabMode = preferences.getInt("tabMode", 1);
            translatorStyle = preferences.getInt("translatorStyle", BaseTranslator.INLINE_STYLE);
            blurIntensity = preferences.getInt("blurIntensity", 75);
            oldBuildVersion = preferences.getString("oldBuildVersion", null);
            stickerSizeStack = preferences.getInt("stickerSizeStack", 14);
            translationProvider = preferences.getInt("translationProvider", isChineseUser ? Translator.PROVIDER_NIU : Translator.PROVIDER_GOOGLE);
            showSantaHat = preferences.getBoolean("showSantaHat", true);
            showSnowFalling = preferences.getBoolean("showSnowFalling", true);
            cameraType = preferences.getInt("cameraType", CameraXUtilities.getDefault());
            cameraXFps = preferences.getInt("cameraXFps", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH ? 60:30);
            useCameraXOptimizedMode = preferences.getBoolean("useCameraXOptimizedMode", SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_HIGH);
            disableProximityEvents = preferences.getBoolean("disableProximityEvents", false);
            swipeToPiP = preferences.getBoolean("swipeToPiP", false);
            verifyLinkTip = preferences.getBoolean("verifyLinkTip", false);
            languagePackVersioning = preferences.getString("languagePackVersioning", "{}");
            xiaomiBlockedInstaller = preferences.getBoolean("xiaomiBlockedInstaller", false);
            increaseAudioMessages = preferences.getBoolean("increaseAudioMessages", false);
            voicesAgc = preferences.getBoolean("voicesAgc", false);
            turnSoundOnVDKey = preferences.getBoolean("turnSoundOnVDKey", true);
            openArchiveOnPull = preferences.getBoolean("openArchiveOnPull", false);
            slidingChatTitle = preferences.getBoolean("slidingChatTitle", false);
            confirmStickersGIFs = preferences.getBoolean("confirmStickersGIFs", false);
            showIDAndDC = preferences.getBoolean("showIDAndDC", true);
            doNotTranslateLanguages = preferences.getString("doNotTranslateLanguages", "[]");
            dcStyleType = preferences.getInt("dcStyleType", 0);
            idType = preferences.getInt("idType", 0);
            searchIconInActionBar = preferences.getBoolean("searchIconInActionBar", false);

            //EXPERIMENTAL OPTIONS
            devOptEnabled = preferences.getBoolean("devOptEnabled", false);

            String dS = devOptEnabled ? "":"_disabled";
            unlimitedFavoriteStickers = preferences.getBoolean("unlimitedFavoriteStickers"+dS, false);
            unlimitedPinnedDialogs = preferences.getBoolean("unlimitedPinnedDialogs"+dS, false);
            maxRecentStickers = preferences.getInt("maxRecentStickers"+dS, 20);
            betterAudioQuality = preferences.getBoolean("betterAudioQuality"+dS, false);

            fixLanguageSelected();
            configLoaded = true;
        }
    }

    public static void toggleHidePhone() {
        hidePhoneNumber = !hidePhoneNumber;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hidePhoneNumber", hidePhoneNumber);
        editor.apply();
    }

    public static void toggleHideContactNumber() {
        hideContactNumber = !hideContactNumber;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideContactNumber", hideContactNumber);
        editor.apply();
    }

    public static void toggleFullTime() {
        fullTime = !fullTime;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("fullTime", fullTime);
        editor.apply();
    }

    public static void toggleRoundedNumbers() {
        roundedNumbers = !roundedNumbers;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("roundedNumbers", roundedNumbers);
        editor.apply();
    }

    public static void toggleConfirmCall() {
        confirmCall = !confirmCall;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("confirmCall", confirmCall);
        editor.apply();
    }

    public static void toggleMediaFlipByTap() {
        mediaFlipByTap = !mediaFlipByTap;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("mediaFlipByTap", mediaFlipByTap);
        editor.apply();
    }

    public static void toggleJumpChannel() {
        jumpChannel = !jumpChannel;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("jumpChannel", jumpChannel);
        editor.apply();
    }

    public static void toggleHideKeyboard() {
        hideKeyboard = !hideKeyboard;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideKeyboard", hideKeyboard);
        editor.apply();
    }

    public static void toggleGifAsVideo() {
        gifAsVideo = !gifAsVideo;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("gifAsVideo", gifAsVideo);
        editor.apply();
    }

    public static void toggleShowFolderWhenForward() {
        showFolderWhenForward = !showFolderWhenForward;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showFolderWhenForward", showFolderWhenForward);
        editor.apply();
    }

    public static void toggleUseRearCamera() {
        useRearCamera = !useRearCamera;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useRearCamera", useRearCamera);
        editor.apply();
    }

    public static void toggleSendConfirm() {
        sendConfirm = !sendConfirm;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sendConfirm", sendConfirm);
        editor.apply();
    }

    public static void toggleUseSystemFont() {
        useSystemFont = !useSystemFont;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemFont", useSystemFont);
        editor.apply();
    }

    public static void toggleUseSystemEmoji() {
        useSystemEmoji = !useSystemEmoji;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemEmoji", useSystemEmoji);
        editor.apply();
    }

    public static void toggleShowGreetings() {
        showGreetings = !showGreetings;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showGreetings", showGreetings);
        editor.apply();
    }

    public static void toggleShowTranslate() {
        showTranslate = !showTranslate;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showTranslate", showTranslate);
        editor.apply();
    }

    public static void toggleShowSaveMessage() {
        showSaveMessage = !showSaveMessage;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showSaveMessage", showSaveMessage);
        editor.apply();
    }

    public static void toggleShowRepeat() {
        showRepeat = !showRepeat;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showRepeat", showRepeat);
        editor.apply();
    }

    public static void toggleShowMessageDetails() {
        showMessageDetails = !showMessageDetails;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showMessageDetails", showMessageDetails);
        editor.apply();
    }

    public static void toggleShowNoQuoteForwardRow() {
        showNoQuoteForward = !showNoQuoteForward;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showNoQuoteForward", showNoQuoteForward);
        editor.apply();
    }

    public static void toggleBetaUpdates() {
        betaUpdates = !betaUpdates;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("betaUpdates", betaUpdates);
        editor.apply();
    }

    public static void toggleNotifyUpdates() {
        notifyUpdates = !notifyUpdates;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifyUpdates", notifyUpdates);
        editor.apply();
    }

    public static void toggleAvatarAsDrawerBackground() {
        avatarAsDrawerBackground = !avatarAsDrawerBackground;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("avatarAsDrawerBackground", avatarAsDrawerBackground);
        editor.apply();
    }

    public static void toggleAvatarBackgroundBlur() {
        avatarBackgroundBlur = !avatarBackgroundBlur;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("avatarBackgroundBlur", avatarBackgroundBlur);
        editor.apply();
    }

    public static void toggleAvatarBackgroundDarken() {
        avatarBackgroundDarken = !avatarBackgroundDarken;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("avatarBackgroundDarken", avatarBackgroundDarken);
        editor.apply();
    }

    public static void toggleShowReportMessage() {
        showReportMessage = !showReportMessage;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showReportMessage", showReportMessage);
        editor.apply();
    }

    public static void toggleBetterAudioQuality() {
        betterAudioQuality = !betterAudioQuality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("betterAudioQuality", betterAudioQuality);
        editor.apply();
    }

    public static void toggleShowGradientColor() {
        showGradientColor = !showGradientColor;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showGradientColor", showGradientColor);
        editor.apply();
    }

    public static void toggleShowAvatarImage() {
        showAvatarImage = !showAvatarImage;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showAvatarImage", showAvatarImage);
        editor.apply();
    }

    public static void toggleOwlEasterSound() {
        owlEasterSound = !owlEasterSound;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("owlEasterSound", owlEasterSound);
        editor.apply();
    }

    public static void togglePacmanForced() {
        pacmanForced = !pacmanForced;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("pacmanForced", pacmanForced);
        editor.apply();
    }

    public static void toggleSmartButtons() {
        smartButtons = !smartButtons;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("smartButtons", smartButtons);
        editor.apply();
    }

    public static void toggleScrollableChatPreview() {
        scrollableChatPreview = !scrollableChatPreview;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("scrollableChatPreview", scrollableChatPreview);
        editor.apply();
    }

    public static void toggleAppBarShadow() {
        disableAppBarShadow = !disableAppBarShadow;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableAppBarShadow", disableAppBarShadow);
        editor.apply();
    }

    public static void toggleAccentColor() {
        accentAsNotificationColor = !accentAsNotificationColor;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("accentAsNotificationColor", accentAsNotificationColor);
        editor.apply();
    }

    public static void toggleShowDeleteDownloadedFile() {
        showDeleteDownloadedFile = !showDeleteDownloadedFile;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showDeleteDownloadedFile", showDeleteDownloadedFile);
        editor.apply();
    }

    public static void toggleHideAllTab() {
        hideAllTab = !hideAllTab;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideAllTab", hideAllTab);
        editor.apply();
    }

    public static void toggleShowSantaHat() {
        showSantaHat = !showSantaHat;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showSantaHat", showSantaHat);
        editor.apply();
    }

    public static void toggleShowSnowFalling() {
        showSnowFalling = !showSnowFalling;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showSnowFalling", showSnowFalling);
        editor.apply();
    }

    public static void toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useCameraXOptimizedMode", useCameraXOptimizedMode);
        editor.apply();
    }

    public static void toggleDisableProximityEvents() {
        disableProximityEvents = !disableProximityEvents;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("disableProximityEvents", disableProximityEvents);
        editor.apply();
    }

    public static void toggleSwipeToPiP() {
        swipeToPiP = !swipeToPiP;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("swipeToPiP", swipeToPiP);
        editor.apply();
    }

    public static void toggleUnlimitedFavoriteStickers() {
        unlimitedFavoriteStickers = !unlimitedFavoriteStickers;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("unlimitedFavoriteStickers", unlimitedFavoriteStickers);
        editor.apply();
    }

    public static void toggleUnlimitedPinnedDialogs() {
        unlimitedPinnedDialogs = !unlimitedPinnedDialogs;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("unlimitedPinnedDialogs", unlimitedPinnedDialogs);
        editor.apply();
    }

    public static void toggleIncreaseAudioMessages() {
        increaseAudioMessages = !increaseAudioMessages;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("increaseAudioMessages", increaseAudioMessages);
        editor.apply();
    }

    public static void toggleVoicesAgc() {
        voicesAgc = !voicesAgc;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("voicesAgc", voicesAgc);
        editor.apply();
    }

    public static void toggleTurnSoundOnVDKey() {
        turnSoundOnVDKey = !turnSoundOnVDKey;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("turnSoundOnVDKey", turnSoundOnVDKey);
        editor.apply();
    }

    public static void toggleOpenArchiveOnPull() {
        openArchiveOnPull = !openArchiveOnPull;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("openArchiveOnPull", openArchiveOnPull);
        editor.apply();
    }

    public static void toggleSlidingChatTitle() {
        slidingChatTitle = !slidingChatTitle;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("slidingChatTitle", slidingChatTitle);
        editor.apply();
    }

    public static void toggleConfirmStickersGIFs() {
        confirmStickersGIFs = !confirmStickersGIFs;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("confirmStickersGIFs", confirmStickersGIFs);
        editor.apply();
    }

    public static void toggleShowIDAndDC() {
        showIDAndDC = !showIDAndDC;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showIDAndDC", showIDAndDC);
        editor.apply();
    }

    public static void toggleSearchIconInActionBar() {
        searchIconInActionBar = !searchIconInActionBar;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("searchIconInActionBar", searchIconInActionBar);
        editor.apply();
    }

    public static void setXiaomiBlockedInstaller() {
        xiaomiBlockedInstaller = true;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("xiaomiBlockedInstaller", xiaomiBlockedInstaller);
        editor.apply();
    }

    public static void setVerifyLinkTip(boolean shown) {
        verifyLinkTip = shown;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("verifyLinkTip", verifyLinkTip);
        editor.apply();
    }

    public static void setTranslationProvider(int provider) {
        translationProvider = provider;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("translationProvider", translationProvider);
        editor.apply();
    }

    public static void setTranslationTarget(String target) {
        translationTarget = target;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("translationTarget", translationTarget);
        editor.apply();
    }

    public static void setTranslationKeyboardTarget(String target) {
        translationKeyboardTarget = target;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("translationKeyboardTarget", translationKeyboardTarget);
        editor.apply();
    }

    public static void setDeepLFormality(int formality) {
        deepLFormality = formality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("deepLFormality", deepLFormality);
        editor.apply();
    }

    public static void setTranslatorStyle(int style) {
        translatorStyle = style;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("translatorStyle", translatorStyle);
        editor.apply();
    }

    public static void setTabMode(int mode) {
        tabMode = mode;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("tabMode", tabMode);
        editor.apply();
    }

    public static void setStickerSize(int size) {
        stickerSizeStack = size;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("stickerSizeStack", stickerSizeStack);
        editor.apply();
    }

    public static void setDcStyleType(int type) {
        dcStyleType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("dcStyleType", dcStyleType);
        editor.apply();
    }

    public static void setIdType(int type) {
        idType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("idType", idType);
        editor.apply();
    }

    public static String currentNotificationVersion() {
        return BuildVars.BUILD_VERSION_STRING + "_" + BuildVars.BUILD_VERSION;
    }

    public static void updateCurrentVersion() {
        oldBuildVersion = currentNotificationVersion();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("oldBuildVersion", oldBuildVersion);
        editor.apply();
    }

    public static void saveLastUpdateCheck() {
        lastUpdateCheck = new Date().getTime();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastUpdateCheck", lastUpdateCheck);
        editor.apply();
    }

    public static void saveUpdateStatus(int status) {
        lastUpdateStatus = status;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastUpdateStatus", lastUpdateStatus);
        editor.apply();
    }

    public static void saveBlurIntensity(int intensity) {
        blurIntensity = intensity;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("blurIntensity", blurIntensity);
        editor.apply();
    }

    public static void remindUpdate(int version) {
        remindedUpdate = version;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("remindedUpdate", remindedUpdate);
        editor.apply();
    }

    public static void saveOldVersion(int version) {
        oldDownloadedVersion = version;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("oldDownloadedVersion", oldDownloadedVersion);
        editor.apply();
    }

    public static void saveButtonStyle(int type) {
        buttonStyleType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("buttonStyleType", buttonStyleType);
        editor.apply();
    }

    public static void saveEventType(int type) {
        eventType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("eventType", eventType);
        editor.apply();
    }

    public static void saveCameraType(int type) {
        cameraType = type;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cameraType", cameraType);
        editor.apply();
    }

    public static void saveCameraXFps(int fps) {
        cameraXFps = fps;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cameraXFps", cameraXFps);
        editor.apply();
    }

    public static void setMaxRecentStickers(int size) {
        maxRecentStickers = size;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("maxRecentStickers", maxRecentStickers);
        editor.apply();
    }

    public static void setUpdateData(String data) {
        updateData = data;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("updateData", updateData);
        editor.apply();
    }

    public static void setDrawerItems(String data) {
        drawerItems = data;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("drawerItems", drawerItems);
        editor.apply();
    }

    public static void setLanguagePackVersioning(String data) {
        languagePackVersioning = data;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("languagePackVersioning", languagePackVersioning);
        editor.apply();
    }

    public static void setDoNotTranslateLanguages(String data) {
        doNotTranslateLanguages = data;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("doNotTranslateLanguages", doNotTranslateLanguages);
        editor.apply();
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

    public static int getActiveAccounts() {
        int accountsNumber = 0;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                accountsNumber++;
            }
        }
        return accountsNumber;
    }

    public static void toggleDevOpt() {
        devOptEnabled = !devOptEnabled;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("devOptEnabled", devOptEnabled);
        editor.apply();
        configLoaded = false;
        loadConfig(false);
    }

    public static boolean isDevOptEnabled() {
        return devOptEnabled || betterAudioQuality || unlimitedFavoriteStickers || unlimitedPinnedDialogs || maxRecentStickers != 20;
    }

    public static boolean canShowFireworks() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int monthOfYear = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return monthOfYear == 1 && dayOfMonth == 1;
    }

    public static void fixLanguageSelected() {
        BaseTranslator translator = Translator.getCurrentTranslator();
        if (translationTarget.equals(translator.getCurrentAppLanguage())) {
            OwlConfig.setTranslationTarget("app");
        }
    }
}