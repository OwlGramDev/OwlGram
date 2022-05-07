package it.owlgram.android.translator;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.owlgram.android.helpers.StandardHTTPRequest;

public class GoogleAppTranslator extends BaseTranslator {

    private static GoogleAppTranslator instance;
    private final List<String> targetLanguages = Arrays.asList(
            "sq", "ar", "am", "az", "ga", "et", "or", "eu", "be", "bg", "is", "pl", "bs",
            "fa", "af", "tt", "da", "de", "ru", "fr", "tl", "fi", "fy", "km", "ka", "gu",
            "kk", "ht", "ko", "ha", "nl", "ky", "gl", "ca", "cs", "kn", "co", "hr", "ku",
            "la", "lv", "lo", "lt", "lb", "rw", "ro", "mg", "mt", "mr", "ml", "ms", "mk",
            "mi", "mn", "bn", "my", "hmn", "xh", "zu", "ne", "no", "pa", "pt", "ps", "ny",
            "ja", "sv", "sm", "sr", "st", "si", "eo", "sk", "sl", "sw", "gd", "ceb", "so",
            "tg", "te", "ta", "th", "tr", "tk", "cy", "ug", "ur", "uk", "uz", "es", "iw",
            "el", "haw", "sd", "hu", "sn", "hy", "ig", "it", "yi", "hi", "su", "id", "jw",
            "en", "yo", "vi", "zh-TW", "zh-CN", "zh");

    private final String[] devices = new String[]{
            "Linux; U; Android 10; Pixel 4",
            "Linux; U; Android 10; Pixel 4 XL",
            "Linux; U; Android 10; Pixel 4a",
            "Linux; U; Android 10; Pixel 4a XL",
            "Linux; U; Android 11; Pixel 4",
            "Linux; U; Android 11; Pixel 4 XL",
            "Linux; U; Android 11; Pixel 4a",
            "Linux; U; Android 11; Pixel 4a XL",
            "Linux; U; Android 11; Pixel 5",
            "Linux; U; Android 11; Pixel 5a",
            "Linux; U; Android 12; Pixel 4",
            "Linux; U; Android 12; Pixel 4 XL",
            "Linux; U; Android 12; Pixel 4a",
            "Linux; U; Android 12; Pixel 4a XL",
            "Linux; U; Android 12; Pixel 5",
            "Linux; U; Android 12; Pixel 5a",
            "Linux; U; Android 12; Pixel 6",
            "Linux; U; Android 12; Pixel 6 Pro",
    };

    static GoogleAppTranslator getInstance() {
        if (instance == null) {
            synchronized (GoogleAppTranslator.class) {
                if (instance == null) {
                    instance = new GoogleAppTranslator();
                }
            }
        }
        return instance;
    }

    @Override
    protected Result translate(String query, String tl) throws IOException, JSONException {
        ArrayList<String> blocks = new ArrayList<>();
        while (query.length() > 2500) {
            String maxBlockStr = query.substring(0, 2500);
            int n;
            n = maxBlockStr.lastIndexOf("\n\n");
            if (n == -1) n = maxBlockStr.lastIndexOf("\n");
            if (n == -1) n = maxBlockStr.lastIndexOf(". ");
            blocks.add(query.substring(0, n + 1));
            query = query.substring(n + 1);
        }
        if (query.length() > 0) {
            blocks.add(query);
        }
        StringBuilder resultString = new StringBuilder();
        String resultLang = "";
        for (int i = 0; i < blocks.size(); i++) {
            String url = "https://translate.googleapis.com/translate_a/single?dj=1" +
                    "&q=" + URLEncoder.encode(blocks.get(i), "UTF-8") +
                    "&sl=auto" +
                    "&tl=" + tl +
                    "&ie=UTF-8&oe=UTF-8&client=at&dt=t&otf=2";
            String response = new StandardHTTPRequest(url)
                    .header("User-Agent", "GoogleTranslate/6.28.0.05.421483610 (" + devices[(int) Math.round(Math.random() * (devices.length - 1))] + ")")
                    .request();
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            Result result = getResult(response);
            if (TextUtils.isEmpty(resultLang)) {
                resultLang = result.sourceLanguage;
            }
            String stringToAdd = ((String) result.translation);
            if (((String) result.translation).length() > 2) {
                if (blocks.get(i).startsWith("\n\n") && !stringToAdd.startsWith("\n\n")) {
                    stringToAdd = "\n\n" + stringToAdd;
                } else if (blocks.get(i).startsWith("\n") && !stringToAdd.startsWith("\n")) {
                    stringToAdd = "\n" + stringToAdd;
                }
                if (blocks.get(i).endsWith("\n\n") && !stringToAdd.endsWith("\n\n")) {
                    stringToAdd += "\n\n";
                } else if (blocks.get(i).endsWith("\n") && !stringToAdd.endsWith("\n")) {
                    stringToAdd += "\n";
                }
            }
            resultString.append(stringToAdd);
        }
        return new Result(
            resultString.toString(),
            resultLang
        );
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
            } else if (languageLowerCase.equals("zh")) {
                if (countryUpperCase.equals("DG")) {
                    code = "zh-CN";
                } else if (countryUpperCase.equals("HK")) {
                    code = "zh-TW";
                } else {
                    code = languageLowerCase;
                }
            } else {
                code = languageLowerCase;
            }
        } else {
            code = languageLowerCase;
        }
        return code;
    }

    private Result getResult(String string) throws JSONException {
        StringBuilder sb = new StringBuilder();
        JSONObject object = new JSONObject(string);
        JSONArray array = object.getJSONArray("sentences");
        for (int i = 0; i < array.length(); i++) {
            sb.append(array.getJSONObject(i).getString("trans"));
        }
        String sourceLang = null;
        try {
            sourceLang = object.getJSONObject("ld_result").getJSONArray("srclangs").getString(0);
        } catch (Exception e) {
            FileLog.e(e, false);
        }
        return new Result(sb.toString(), sourceLang);
    }
}

