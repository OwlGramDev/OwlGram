package it.owlgram.android;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

import java.util.Date;

import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;

public class OwlConfig {
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
    public static boolean showFolderIcons;
    public static boolean useRearCamera;
    public static boolean sendConfirm;
    public static boolean useSystemFont;
    public static boolean useSystemEmoji;
    public static boolean showGreetings;
    public static boolean showTranslate;
    public static boolean showSaveMessage;
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
    public static boolean isChineseUser = false;
    public static String translationTarget = "app";
    public static String updateData;
    public static int deepLFormality = DeepLTranslator.FORMALITY_DEFAULT;
    public static int translationProvider = Translator.PROVIDER_GOOGLE;
    public static int lastUpdateStatus = 0;
    public static int remindedUpdate = 0;
    public static long lastUpdateCheck = 0;
    public static int oldDownloadedVersion = 0;
    public static int blurIntensity = 0;


    private static boolean configLoaded;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
            isChineseUser = ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isChineseUser);
            hidePhoneNumber = preferences.getBoolean("hidePhoneNumber", true);
            hideContactNumber = preferences.getBoolean("hideContactNumber", true);
            fullTime = preferences.getBoolean("fullTime", false);
            roundedNumbers = preferences.getBoolean("roundedNumbers", true);
            confirmCall = preferences.getBoolean("confirmCall", true);
            mediaFlipByTap = preferences.getBoolean("mediaFlipByTap", false);
            jumpChannel = preferences.getBoolean("jumpChannel", false);
            hideKeyboard = preferences.getBoolean("hideKeyboard", false);
            gifAsVideo = preferences.getBoolean("gifAsVideo", true);
            showFolderWhenForward = preferences.getBoolean("showFolderWhenForward", true);
            //showFolderIcons = preferences.getBoolean("showFolderIcons", true);
            useRearCamera = preferences.getBoolean("useRearCamera", false);
            sendConfirm = preferences.getBoolean("sendConfirm", false);
            useSystemFont = preferences.getBoolean("useSystemFont", false);
            useSystemEmoji = preferences.getBoolean("useSystemEmoji", false);
            showGreetings = preferences.getBoolean("showGreetings", true);
            showTranslate = preferences.getBoolean("showTranslate", true);
            showSaveMessage = preferences.getBoolean("showSaveMessage", true);
            showNoQuoteForward = preferences.getBoolean("showNoQuoteForward", false);
            showMessageDetails = preferences.getBoolean("showMessageDetails", false);
            betaUpdates = preferences.getBoolean("betaUpdates", false);
            notifyUpdates = preferences.getBoolean("notifyUpdates", true);
            avatarBackgroundDarken = preferences.getBoolean("avatarBackgroundDarken", false);
            avatarBackgroundBlur = preferences.getBoolean("avatarBackgroundBlur", false);
            avatarAsDrawerBackground = preferences.getBoolean("avatarBackgroundBlur", false);
            betterAudioQuality = preferences.getBoolean("betterAudioQuality", false);
            showReportMessage = preferences.getBoolean("showReportMessage", true);
            showGradientColor = preferences.getBoolean("showGradientColor", false);
            showAvatarImage = preferences.getBoolean("showAvatarImage", true);
            lastUpdateCheck = preferences.getLong("lastUpdateCheck", 0);
            lastUpdateStatus = preferences.getInt("lastUpdateStatus", 0);
            remindedUpdate = preferences.getInt("remindedUpdate", 0);
            translationTarget = preferences.getString("translationTarget", "app");
            updateData = preferences.getString("updateData", "");
            oldDownloadedVersion = preferences.getInt("oldDownloadedVersion", 0);
            blurIntensity = preferences.getInt("blurIntensity", 75);
            translationProvider = preferences.getInt("translationProvider", isChineseUser ? Translator.PROVIDER_NIU : Translator.PROVIDER_GOOGLE);
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

    public static void setDeepLFormality(int formality) {
        deepLFormality = formality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("deepLFormality", deepLFormality);
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

    public static void setUpdateData(String data) {
        updateData = data;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("updateData", updateData);
        editor.apply();
    }

}