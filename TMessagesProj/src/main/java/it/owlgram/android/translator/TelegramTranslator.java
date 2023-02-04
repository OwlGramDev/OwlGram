package it.owlgram.android.translator;

import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TelegramTranslator extends BaseTranslator {

    private final List<String> targetLanguages = GoogleAppTranslator.getInstance().getTargetLanguages();

    private static final class InstanceHolder {
        private static final TelegramTranslator instance = new TelegramTranslator();
    }

    static TelegramTranslator getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    protected Result translate(String query, String tl) throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        AtomicReference<String> detectedLanguage = new AtomicReference<>();
        final CountDownLatch waitDetect = new CountDownLatch(1);

        if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(query, lng -> {
                if (!Objects.equals(lng, "und")) {
                    detectedLanguage.set(lng);
                }
                waitDetect.countDown();
            }, e -> {
                exception.set(e);
                waitDetect.countDown();
            });
            waitDetect.await();
            if (exception.get() != null) {
                throw exception.get();
            }
        }

        ArrayList<String> blocks = getStringBlocks(query, 2500);
        StringBuilder resultString = new StringBuilder();

        for (String block : blocks) {
            final CountDownLatch waitTranslate = new CountDownLatch(1);
            AtomicReference<String> translated = new AtomicReference<>();

            TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
            req.flags |= 2;
            req.to_lang = tl;
            // TODO: ADD KEEP_FORMATTING FLAG
            /*req.text = block.replace("\n", "<br>");
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> {
                if (res instanceof TLRPC.TL_messages_translateResultText) {
                    TLRPC.TL_messages_translateResultText result = (TLRPC.TL_messages_translateResultText) res;
                    translated.set(result.text);
                } else if (err != null) {
                    exception.set(new Exception(err.text));
                } else {
                    exception.set(new Exception("Unknown error"));
                }
                waitTranslate.countDown();
            });
            waitTranslate.await();
            if (exception.get() != null) {
                throw exception.get();
            }
            resultString.append(buildTranslatedString(block, translated.get().replace("<br>", "\n")));*/
        }
        return new Result(resultString.toString(), detectedLanguage.get());
    }

    @Override
    public String convertLanguageCode(String language, String country) {
        return GoogleAppTranslator.getInstance().convertLanguageCode(language, country);
    }

    @Override
    public List<String> getTargetLanguages() {
        return targetLanguages;
    }
}
