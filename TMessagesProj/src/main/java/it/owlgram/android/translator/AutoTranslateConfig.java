package it.owlgram.android.translator;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.ApplicationLoader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static Map<Long, Boolean> getAllConfigs() {
        return preferences.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("autoTranslate_"))
                .collect(
                        HashMap::new,
                        (map, entry) -> map.put(Long.parseLong(entry.getKey().split("_")[1]), (Boolean) entry.getValue()),
                        HashMap::putAll
                );
    }

    public static int getExceptions() {
        return getAllConfigs().size();
    }

    public static int getAlwaysExceptions() {
        return (int) getAllConfigs().entrySet().stream().filter(Map.Entry::getValue).count();
    }

    public static int getNeverExceptions() {
        return (int) getAllConfigs().entrySet().stream().filter(entry -> !entry.getValue()).count();
    }

    public static boolean resetExceptions() {
        return preferences.edit().clear().commit();
    }
}