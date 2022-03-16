package org.telegram.messenger;

import android.content.Context;

import com.google.mlkit.common.MlKit;

public class LanguageDetector {
    public interface StringCallback {
        void run(String str);
    }
    public interface ExceptionCallback {
        void run(Exception e);
    }

    public static boolean hasSupport() {
        return true;
    }

    public static void detectLanguage(Context context, String text, StringCallback onSuccess, ExceptionCallback onFail) {
        try {
            com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
                    .identifyLanguage(text)
                    .addOnSuccessListener(onSuccess::run)
                    .addOnFailureListener(onFail::run);
        } catch (IllegalStateException e) {
            if (context != null) {
                MlKit.initialize(context);
                detectLanguage(null, text, onSuccess, onFail);
            } else {
                onFail.run(e);
            }
        }
    }
}