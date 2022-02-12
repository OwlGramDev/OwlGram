package it.owlgram.android.translator;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import it.owlgram.android.helpers.StandardHTTPRequest;

public class NiuTranslator extends BaseTranslator {

    private static NiuTranslator instance;
    private final List<String> targetLanguages = Arrays.asList(
            "sq", "ar", "am", "az", "ga", "et", "ee", "om", "or", "os", "aym", "fa", "br",
            "tpi", "ba", "eu", "be", "bg", "is", "bi", "bem", "pl", "bs", "bam", "ku", "crh",
            "cha", "cv", "tn", "ts", "che", "xal", "ccp", "tt", "da", "de", "tet", "dv", "dje",
            "dyu", "ru", "fr", "fo", "fil", "fj", "fi", "km", "kg", "fy", "gu", "ka", "kk",
            "ht", "ko", "ha", "nl", "ky", "quc", "gl", "ca", "cs", "gil", "kac", "kab", "kn",
            "cop", "co", "hr", "la", "lv", "lo", "rn", "lt", "ln", "lg", "lb", "rw", "ro",
            "rug", "mg", "mt", "gv", "mr", "ml", "ms", "mk", "mi", "mn", "my", "bn", "mni",
            "mah", "mad", "mos", "af", "xh", "zu", "ne", "no", "pap", "pa", "pt", "ps", "ny",
            "tw", "chr", "ja", "sv", "sm", "sr", "crs", "st", "sg", "si", "mrj", "eo", "sk",
            "sl", "sw", "gd", "so", "jmc", "tg", "ty", "te", "ta", "th", "to", "tig", "tmh",
            "tr", "tk", "tyv", "teo", "wal", "war", "cy", "ve", "wol", "udm", "ur", "uk", "uz",
            "vun", "es", "he", "shi", "el", "haw", "sd", "hu", "sn", "ceb", "syc", "hmo", "sid",
            "hy", "ace", "ig", "it", "yi", "hi", "su", "id", "jv", "en", "yo", "vi", "yue",
            "dz", "dtp", "zh-TW", "zh-CN");

    static NiuTranslator getInstance() {
        if (instance == null) {
            synchronized (NiuTranslator.class) {
                if (instance == null) {
                    instance = new NiuTranslator();
                }
            }
        }
        return instance;
    }

    @Override
    protected Result translate(String query, String tl) throws IOException, JSONException {
        if (tl.equals("zh-CN")) {
            tl = "zh";
        } else if (tl.equals("zh-TW")) {
            tl = "cht";
        }
        String url = "https://test.niutrans.com/NiuTransServer/testaligntrans?" +
                "from=auto" +
                "&to=" + tl +
                "&src_text=" + URLEncoder.encode(query, "UTF-8") +
                "&source=text&dictNo=&memoryNo=&isUseDict=0&isUseMemory=0&time=" + System.currentTimeMillis();
        String response = new StandardHTTPRequest(url)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A5297c Safari/602.1")
                .request();
        if (TextUtils.isEmpty(response)) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(response);
        if (!jsonObject.has("tgt_text") && jsonObject.has("error_msg")) {
            throw new IOException(jsonObject.getString("error_msg"));
        }
        return new Result(jsonObject.getString("tgt_text"), jsonObject.getString("from"));
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
}

