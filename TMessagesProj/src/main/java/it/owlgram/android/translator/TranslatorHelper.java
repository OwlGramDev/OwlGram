package it.owlgram.android.translator;

import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.entities.HTMLKeeper;
import it.owlgram.android.helpers.MessageHelper;

public class TranslatorHelper {

    public static void resetTranslatedMessage(MessageObject messageObject) {
        resetTranslatedMessage(messageObject, false);
    }

    public static void resetTranslatedMessage(MessageObject messageObject, boolean noCache) {
        if (noCache) {
            messageObject.messageOwner.translatedText = null;
            messageObject.translatedPoll = null;
        } else {
            MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().hideTranslation(messageObject);
        }
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.messageTranslated, messageObject);
    }

    public static boolean isSupportHTMLMode() {
        return isSupportHTMLMode(OwlConfig.translationProvider);
    }

    public static boolean isSupportHTMLMode(int provider) {
        return provider == Translator.PROVIDER_GOOGLE ||
                provider == Translator.PROVIDER_YANDEX ||
                provider == Translator.PROVIDER_DEEPL;
    }

    public static boolean isSupportAutoTranslate() {
        return isSupportAutoTranslate(OwlConfig.translationProvider);
    }

    public static boolean isSupportAutoTranslate(int provider) {
        return provider == Translator.PROVIDER_GOOGLE ||
                provider == Translator.PROVIDER_YANDEX ||
                provider == Translator.PROVIDER_DEEPL ||
                provider == Translator.PROVIDER_DUCKDUCKGO;
    }

    public static class TranslatorContext {
        private final Object translateObject;
        private int symbolsCount = 0;

        public TranslatorContext(MessageObject messageObject) {
            BaseTranslator.AdditionalObjectTranslation additionalObjectTranslation = new BaseTranslator.AdditionalObjectTranslation();
            if (messageObject.type == MessageObject.TYPE_POLL) {
                if (messageObject.originalPoll == null) {
                    messageObject.originalPoll = new MessageHelper.PollTexts(((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll);
                }
                additionalObjectTranslation.translation = messageObject.originalPoll.copy();
                additionalObjectTranslation.messagesCount = messageObject.originalPoll.size();
            } else {
                additionalObjectTranslation.translation = messageObject.messageOwner.message;
                additionalObjectTranslation.messagesCount = 1;
            }
            if (messageObject.messageOwner.reply_markup != null && messageObject.messageOwner.reply_markup.rows.size() > 0) {
                if (messageObject.originalReplyMarkupRows == null) {
                    messageObject.originalReplyMarkupRows = new MessageHelper.ReplyMarkupButtonsTexts(messageObject.messageOwner.reply_markup.rows);
                }
                additionalObjectTranslation.additionalInfo = messageObject.originalReplyMarkupRows.copy();
                additionalObjectTranslation.messagesCount += messageObject.originalReplyMarkupRows.size();
            }
            if (additionalObjectTranslation.translation instanceof String) {
                symbolsCount = ((String) additionalObjectTranslation.translation).length();
            } else if (additionalObjectTranslation.translation instanceof MessageHelper.PollTexts) {
                MessageHelper.PollTexts poll = (MessageHelper.PollTexts) additionalObjectTranslation.translation;
                for (int a = 0; a < poll.getTexts().size(); a++) {
                    symbolsCount += poll.getTexts().get(a).length();
                }
            }
            if (messageObject.messageOwner.entities != null && additionalObjectTranslation.translation instanceof String) {
                if (OwlConfig.keepTranslationMarkdown) {
                    if (isSupportHTMLMode()) {
                        additionalObjectTranslation.translation = HTMLKeeper.entitiesToHtml((String) additionalObjectTranslation.translation, messageObject.messageOwner.entities, false);
                    } else if (OwlConfig.translationProvider == Translator.PROVIDER_TELEGRAM) { // Use Telegram entities
                        TLRPC.TL_textWithEntities source = new TLRPC.TL_textWithEntities();
                        source.text = messageObject.messageOwner.message;
                        source.entities = messageObject.messageOwner.entities;
                        additionalObjectTranslation.translation = source;
                    }
                }
            }
            translateObject = additionalObjectTranslation;
        }

        public int getSymbolsCount() {
            return symbolsCount;
        }

        public Object getTranslateObject() {
            return translateObject;
        }
    }

    public static boolean isTranslating(MessageObject messageObject) {
        return MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().isTranslating(messageObject);
    }

    public static String languageName(String code) {
        Locale language = Locale.forLanguageTag(code);
        String fromString = !TextUtils.isEmpty(language.getScript()) ? String.valueOf(HtmlCompat.fromHtml(language.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY)) : language.getDisplayName();
        return AndroidUtilities.capitalize(fromString);
    }
}
