package it.owlgram.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;

@SuppressLint("ApplySharedPref")
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
    public static boolean separatedPhotoAndVideo;
    public static boolean showFolderWhenForward;
    public static boolean showFolderIcons;
    public static boolean useRearCamera;
    public static boolean sendConfirm;
    public static boolean useSystemFont;
    public static boolean useSystemEmoji;
    public static String translationTarget = "app";
    public static int deepLFormality = DeepLTranslator.FORMALITY_DEFAULT;
    public static int translationProvider = Translator.PROVIDER_GOOGLE;
    public static boolean isChineseUser = false;

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
            mediaFlipByTap = preferences.getBoolean("mediaSwipeByTap", true);
            jumpChannel = preferences.getBoolean("jumpChannel", true);
            hideKeyboard = preferences.getBoolean("hideKeyboard", false);
            gifAsVideo = preferences.getBoolean("gifAsVideo", true);
            separatedPhotoAndVideo = preferences.getBoolean("separatedPhotoAndVideo", false);
            showFolderWhenForward = preferences.getBoolean("showFolderWhenForward", false);
            showFolderIcons = preferences.getBoolean("showFolderWhenForward", false);
            useRearCamera = preferences.getBoolean("useRearCamera", false);
            sendConfirm = preferences.getBoolean("sendConfirm", false);
            useSystemFont = preferences.getBoolean("useSystemFont", false);
            useSystemEmoji = preferences.getBoolean("useSystemEmoji", false);
            translationTarget = preferences.getString("translationTarget", "app");
            translationProvider = preferences.getInt("translationProvider", isChineseUser ? Translator.PROVIDER_NIU : Translator.PROVIDER_GOOGLE);
            configLoaded = true;
        }
    }

    public static void toggleHidePhone() {
        hidePhoneNumber = !hidePhoneNumber;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hidePhone", hidePhoneNumber);
        editor.commit();
    }

    public static void toggleHideContactNumber() {
        hideContactNumber = !hideContactNumber;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideContactNumber", hideContactNumber);
        editor.commit();
    }

    public static void toggleFullTime() {
        fullTime = !fullTime;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("fullTime", fullTime);
        editor.commit();
    }

    public static void toggleRoundedNumbers() {
        roundedNumbers = !roundedNumbers;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("roundedNumbers", roundedNumbers);
        editor.commit();
    }

    public static void toggleConfirmCall() {
        confirmCall = !confirmCall;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("confirmCall", confirmCall);
        editor.commit();
    }

    public static void toggleMediaFlipByTap() {
        mediaFlipByTap = !mediaFlipByTap;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("mediaFlipByTap", mediaFlipByTap);
        editor.commit();
    }

    public static void toggleJumpChannel() {
        jumpChannel = !jumpChannel;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("jumpChannel", jumpChannel);
        editor.commit();
    }

    public static void toggleHideKeyboard() {
        hideKeyboard = !hideKeyboard;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hideKeyboard", hideKeyboard);
        editor.commit();
    }

    public static void toggleGifAsVideo() {
        gifAsVideo = !gifAsVideo;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("gifAsVideo", gifAsVideo);
        editor.commit();
    }

    public static void toggleShowFolderWhenForward() {
        showFolderWhenForward = !showFolderWhenForward;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("showFolderWhenForward", showFolderWhenForward);
        editor.commit();
    }

    public static void toggleUseRearCamera() {
        useRearCamera = !useRearCamera;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useRearCamera", useRearCamera);
        editor.commit();
    }

    public static void toggleSendConfirm() {
        sendConfirm = !sendConfirm;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sendConfirm", sendConfirm);
        editor.commit();
    }

    public static void toggleUseSystemFont() {
        useSystemFont = !useSystemFont;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemFont", useSystemFont);
        editor.commit();
    }

    public static void toggleUseSystemEmoji() {
        useSystemEmoji = !useSystemEmoji;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSystemEmoji", useSystemEmoji);
        editor.commit();
    }

    public static void setTranslationProvider(int provider) {
        translationProvider = provider;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("translationProvider", translationProvider);
        editor.commit();
    }

    public static void setTranslationTarget(String target) {
        translationTarget = target;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("translationTarget", translationTarget);
        editor.commit();
    }

    public static void setDeepLFormality(int formality) {
        deepLFormality = formality;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("deepLFormality", deepLFormality);
        editor.commit();
    }

}