package org.telegram.messenger;

import java.util.Arrays;
import java.util.List;

public class LanguageDetector {
    private static Boolean hasSupport = null;

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
        return hasSupport(false);
    }

    private static boolean hasSupport(boolean initializeFirst) {
        if (hasSupport == null) {
            try {
                if (initializeFirst) {
                    com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(ApplicationLoader.applicationContext);
                }
                com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                        .identifyLanguage("apple")
                        .addOnSuccessListener(str -> {
                        })
                        .addOnFailureListener(e -> {
                        });
                hasSupport = true;
            } catch (Throwable t) {
                FileLog.e(t);
                if (initializeFirst) {
                    hasSupport = false;
                } else {
                    return hasSupport(true);
                }
            }
        }
        return hasSupport;
    }

    public static void detectLanguage(String text, StringCallback onSuccess, ExceptionCallback onFail) {
        detectLanguage(text, onSuccess, onFail, false);
    }

    public static void detectLanguage(String text, StringCallback onSuccess, ExceptionCallback onFail, boolean initializeFirst) {
        try {
            if (initializeFirst) {
                com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(ApplicationLoader.applicationContext);
            }
            com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                .identifyLanguage(text)
                .addOnSuccessListener(str -> {
                    if (onSuccess != null) {
                        onSuccess.run(str);
                    }
                })
                .addOnFailureListener(e -> {
                    if (onFail != null) {
                        onFail.run(e);
                    }
                });
        } catch (IllegalStateException e) {
            if (!initializeFirst) {
                detectLanguage(text, onSuccess, onFail, true);
            } else {
                if (onFail != null) {
                    onFail.run(e);
                }
                FileLog.e(e, false);
            }
        } catch (Exception e) {
            if (onFail != null) {
                onFail.run(e);
            }
            FileLog.e(e);
        } catch (Throwable t) {
            if (onFail != null) {
                onFail.run(null);
            }
            FileLog.e(t, false);
        }
    }
}