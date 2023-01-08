package it.owlgram.android.helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

import java.util.Map;

public class SharedPreferencesHelper {
    private static final SharedPreferences preferences;

    static {
        preferences = ApplicationLoader.applicationContext.getSharedPreferences("owlgram", Activity.MODE_PRIVATE);
    }

    protected static void putValue(String key, Object value) {
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.apply();
    }

    protected static String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    protected static int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    protected static boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    protected static long getLong(String key, long defaultValue) {
        try {
            return preferences.getLong(key, defaultValue);
        } catch (ClassCastException e) {
            return getInt(key, (int) defaultValue);
        }
    }

    protected static Map<String, ?> getAll() {
        return preferences.getAll();
    }
    
    protected static void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    protected static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
