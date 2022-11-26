package it.owlgram.android.translator;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

import it.owlgram.android.OwlConfig;

public class AutoTranslateConfig {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OwlDialogConfig", Context.MODE_PRIVATE);

    public static boolean isAutoTranslateEnabled(long dialog_id, int topicId) {
        return preferences.getBoolean("autoTranslate_" + dialog_id + (topicId != 0 ? "_" + topicId : ""), OwlConfig.autoTranslate);
    }

    public static boolean hasAutoTranslateConfig(long dialog_id, int topicId) {
        return preferences.contains("autoTranslate_" + dialog_id + (topicId != 0 ? "_" + topicId : ""));
    }

    public static void setAutoTranslateEnable(long dialog_id, int topicId, boolean enable) {
        preferences.edit().putBoolean("autoTranslate_" + dialog_id + (topicId != 0 ? "_" + topicId : ""), enable).apply();
    }

    public static void removeAutoTranslateConfig(long dialog_id, int topicId) {
        preferences.edit().remove("autoTranslate_" + dialog_id + (topicId != 0 ? "_" + topicId : "")).apply();
    }

    public static boolean resetAutoTranslateConfigs() {
        return preferences.edit().clear().commit();
    }
}