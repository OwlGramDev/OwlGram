package it.owlgram.android.translator;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;

import java.util.List;
import java.util.Locale;

import it.owlgram.android.OwlConfig;


abstract public class BaseTranslator {

    public static final int INLINE_STYLE = 0;
    public static final int DIALOG_STYLE = 1;

    abstract protected Result translate(String query, String tl) throws Exception;

    abstract public List<String> getTargetLanguages();

    public String convertLanguageCode(String language, String country) {
        return language;
    }

    public void startTask(Object query, String toLang, Translator.TranslateCallBack translateCallBack) {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (query instanceof CharSequence) {
                        Result result = translate(query.toString(), toLang);
                        if (result != null) {
                            AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(result));
                        } else {
                            AndroidUtilities.runOnUIThread(() -> translateCallBack.onError(null));
                        }
                    } else if (query instanceof TLRPC.Poll) {
                        TLRPC.TL_poll poll = new TLRPC.TL_poll();
                        TLRPC.TL_poll original = (TLRPC.TL_poll) query;
                        poll.question = (String) translate(original.question, toLang).translation;
                        for (int i = 0; i < original.answers.size(); i++) {
                            TLRPC.TL_pollAnswer answer = new TLRPC.TL_pollAnswer();
                            answer.text = (String) translate(original.answers.get(i).text, toLang).translation;
                            answer.option = original.answers.get(i).option;
                            poll.answers.add(answer);
                        }
                        poll.close_date = original.close_date;
                        poll.close_period = original.close_period;
                        poll.closed = original.closed;
                        poll.flags = original.flags;
                        poll.id = original.id;
                        poll.multiple_choice = original.multiple_choice;
                        poll.public_voters = original.public_voters;
                        poll.quiz = original.quiz;
                        AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(new Result(poll, null)));
                    } else {
                        throw new UnsupportedOperationException("Unsupported translation query");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FileLog.e(e, false);
                    AndroidUtilities.runOnUIThread(() -> translateCallBack.onError(e));
                }
            }
        }.start();
    }

    public boolean supportLanguage(String language) {
        return getTargetLanguages().contains(language);
    }

    public String getCurrentAppLanguage() {
        String toLang;
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        toLang = convertLanguageCode(locale.getLanguage(), locale.getCountry());
        if (!supportLanguage(toLang)) {
            toLang = convertLanguageCode(LocaleController.getString("LanguageCode", R.string.LanguageCode), null);
        }
        return toLang;
    }

    public String getTargetLanguage(String language) {
        String toLang;
        if (language.equals("app")) {
            toLang = getCurrentAppLanguage();
        } else {
            toLang = language;
        }
        return toLang;
    }

    public String getCurrentTargetLanguage() {
        return getTargetLanguage(OwlConfig.translationTarget);
    }

    public static class Result {
        public Object translation;
        @Nullable
        public String sourceLanguage;

        public Result(Object translation, @Nullable String sourceLanguage) {
            this.translation = translation;
            this.sourceLanguage = sourceLanguage;
        }
    }
}
