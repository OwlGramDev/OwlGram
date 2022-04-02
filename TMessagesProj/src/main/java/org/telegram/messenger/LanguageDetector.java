package org.telegram.messenger;

import java.util.Arrays;
import java.util.List;

public class LanguageDetector {
    public static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "co", "cs", "cy", "da", "de", "el", "en", "eo",
            "es", "et", "eu", "fa", "fil", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "he", "hi", "hmn", "hr", "ht",
            "hu", "hy", "id", "ig", "is", "it", "ja", "jv", "ka", "kk", "km", "kn", "ko", "ku", "ky", "la", "lb", "lo",
            "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa", "pl", "ps",
            "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "st", "su", "sv", "sw", "ta", "te",
            "tg", "th", "tr", "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zh", "zu"
    );
    public interface StringCallback {
        void run(String str);
    }
    public interface ExceptionCallback {
        void run(Exception e);
    }

    public static boolean hasSupport() {
        return true;
    }

    public static void detectLanguage(String text, StringCallback onSuccess, ExceptionCallback onFail) {
        try {
            com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                    .identifyLanguage(text)
                    .addOnSuccessListener(str -> {
                        onSuccess.run(str);
                    })
                    .addOnFailureListener(e -> {
                        onFail.run(e);
                    });
        } catch (Exception e) {
            FileLog.e(e);
            onFail.run(e);
        }
    }
}