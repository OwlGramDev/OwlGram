package it.owlgram.android.translator;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.translator.raw.RawDeepLTranslator;

public class DeepLTranslator extends BaseTranslator {

    public static final int FORMALITY_DEFAULT = 0;
    public static final int FORMALITY_MORE = 1;
    public static final int FORMALITY_LESS = 2;

    private final List<String> targetLanguages = Arrays.asList(
            "bg", "pl", "da", "de", "ru", "fr", "fi", "nl", "cs", "lv", "lt", "ro",
            "pt", "pt-PT", "pt-BR", "ja", "sv", "sk", "sl", "es", "el", "hu", "it", "en", "en-GB", "en-US", "zh");
    private final RawDeepLTranslator deeplTranslator = new RawDeepLTranslator();

    private static final class InstanceHolder {
        private static final DeepLTranslator instance = new DeepLTranslator();
    }

    static DeepLTranslator getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public List<String> getTargetLanguages() {
        return targetLanguages;
    }

    @Override
    public String convertLanguageCode(String language, String country) {
        String languageLowerCase = language.toLowerCase();
        String code;
        if (!TextUtils.isEmpty(country)) {
            String countryUpperCase = country.toUpperCase();
            if (targetLanguages.contains(languageLowerCase + "-" + countryUpperCase)) {
                code = languageLowerCase + "-" + countryUpperCase;
            } else {
                code = languageLowerCase;
            }
        } else {
            code = languageLowerCase;
        }
        return code;
    }

    @Override
    protected Result translate(String query, String tl) throws Exception {
        String[] result = deeplTranslator.translate(query, tl, getFormalityString(), "newlines");
        return new Result(result[1], result[0]);
    }

    private String getFormalityString() {
        switch (OwlConfig.deepLFormality) {
            case FORMALITY_DEFAULT:
            default:
                return null;
            case FORMALITY_MORE:
                return "formal";
            case FORMALITY_LESS:
                return "informal";
        }
    }
}

