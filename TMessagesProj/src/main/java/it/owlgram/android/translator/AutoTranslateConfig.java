package it.owlgram.android.translator;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

import it.owlgram.android.OwlConfig;

public class AutoTranslateConfig {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OwlDialogConfig", Context.MODE_PRIVATE);

    public static boolean isAutoTranslateEnabled(long dialog_id) {
        return preferences.getBoolean("autoTranslate_" + dialog_id, OwlConfig.autoTranslate);
    }

    public static boolean hasAutoTranslateConfig(long dialog_id) {
        return preferences.contains("autoTranslate_" + dialog_id);
    }

    public static void setAutoTranslateEnable(long dialog_id, boolean enable) {
        preferences.edit().putBoolean("autoTranslate_" + dialog_id, enable).apply();
    }

    public static void removeAutoTranslateConfig(long dialog_id) {
        preferences.edit().remove("autoTranslate_" + dialog_id).apply();
    }
}