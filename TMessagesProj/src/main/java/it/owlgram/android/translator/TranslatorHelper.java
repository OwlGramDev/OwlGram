package it.owlgram.android.translator;

import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.entities.HTMLKeeper;
import it.owlgram.android.helpers.MessageHelper;
import it.owlgram.android.settings.DoNotTranslateSettings;

public class TranslatorHelper {

    private static final ArrayList<String> translatingIDs = new ArrayList<>();

    public static boolean isTranslating(String uid) {
        return translatingIDs.contains(uid);
    }

    public static void translate(TranslatorContext translatorContext, TranslateCallback listener) {
        if (translatingIDs.contains(translatorContext.UID)) return;
        translatingIDs.add(translatorContext.UID);
        listener.onPreTranslate();
        Translator.translate(translatorContext.translateObject, new Translator.TranslateCallBack() {
            @Override
            public void onSuccess(BaseTranslator.Result result) {
                translatingIDs.remove(translatorContext.UID);
                listener.onTranslate(result);
            }

            @Override
            public void onError(Exception e) {
                translatingIDs.remove(translatorContext.UID);
                listener.onError(e);
            }
        });
    }

    public static MessageObject applyTranslatedMessage(BaseTranslator.Result result, MessageObject messageObject, long dialog_id, BaseFragment fragment, boolean autoTranslate) {
        if (result.sourceLanguage != null) {
            String src_lang = result.sourceLanguage.toUpperCase();
            String language = Translator.getTranslator(OwlConfig.translationProvider).getCurrentTargetLanguage().toUpperCase();
            if (result.translation instanceof String) {
                if(messageObject.originalEntities != null){
                    Pair<String, ArrayList<TLRPC.MessageEntity>> entitiesResult = HTMLKeeper.htmlToEntities((String) result.translation, messageObject.originalEntities, !isSupportHTMLMode());
                    if (autoTranslate && (entitiesResult.first.equalsIgnoreCase(messageObject.originalMessage.toString()) || DoNotTranslateSettings.getRestrictedLanguages().contains(src_lang.toLowerCase()))) {
                        messageObject.translating = false;
                        messageObject.translated = false;
                        messageObject.canceledTranslation = true;
                    } else {
                        messageObject.messageOwner.message = entitiesResult.first;
                        messageObject.messageOwner.entities = entitiesResult.second;
                        messageObject.translated = true;
                        messageObject.translatedLanguage = Pair.create(src_lang, language);
                        if (result.additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                            ((MessageHelper.ReplyMarkupButtonsTexts) result.additionalInfo).applyTextToKeyboard(messageObject.messageOwner.reply_markup.rows);
                        }
                        if (fragment == null) {
                            messageObject.translating = false;
                            messageObject.caption = null;
                            messageObject.generateCaption();
                        }
                    }
                }
            } else if (result.translation instanceof TLRPC.TL_poll) {
                messageObject.translated = true;
                messageObject.translatedLanguage = Pair.create(src_lang, language);
                ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll = (TLRPC.TL_poll) result.translation;
            }
        } else {
            messageObject.translating = false;
            messageObject.translated = false;
            messageObject.canceledTranslation = true;
            messageObject.translatedLanguage = null;
        }
        if (fragment != null) {
            AndroidUtilities.runOnUIThread(() ->  fragment.getMessageHelper().resetMessageContent(dialog_id, messageObject, messageObject.translated, messageObject.canceledTranslation));
        }
        return messageObject;
    }

    public static MessageObject applyTranslatedMessage(BaseTranslator.Result result, MessageObject messageObject) {
        return applyTranslatedMessage(result, messageObject, 0, null, false);
    }

    public static MessageObject resetTranslatedMessage(long dialog_id, BaseFragment fragment, MessageObject messageObject) {
        if (messageObject.originalMessage instanceof String) {
            messageObject.messageOwner.message = (String) messageObject.originalMessage;
            messageObject.messageText = messageObject.messageOwner.message;
            if(messageObject.originalEntities != null){
                messageObject.messageOwner.entities = new ArrayList<>(messageObject.originalEntities);
            }
            if(messageObject.originalReplyMarkupRows != null){
                ((MessageHelper.ReplyMarkupButtonsTexts) messageObject.originalReplyMarkupRows).applyTextToKeyboard(messageObject.messageOwner.reply_markup.rows);
            }
        } else if (messageObject.originalMessage instanceof TLRPC.TL_poll) {
            ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll = (TLRPC.TL_poll) messageObject.originalMessage;
        }
        messageObject.translatedLanguage = null;
        return fragment.getMessageHelper().resetMessageContent(dialog_id, messageObject, false, true);
    }

    public static MessageObject resetTranslatedCaption(MessageObject messageObject) {
        if (messageObject.originalMessage instanceof String) {
            messageObject.messageOwner.message = (String) messageObject.originalMessage;
            messageObject.translating = false;
            messageObject.translated = false;
            messageObject.caption = null;
            messageObject.generateCaption();
        }
        return messageObject;
    }

    public static boolean isSupportHTMLMode() {
        return isSupportHTMLMode(OwlConfig.translationProvider);
    }

    public static boolean isSupportHTMLMode(int provider) {
        return provider == Translator.PROVIDER_GOOGLE ||
                provider == Translator.PROVIDER_YANDEX ||
                provider == Translator.PROVIDER_DEEPL;
    }

    private static boolean isSupportedProvider() {
        return isSupportHTMLMode() || Translator.isSupportedOutputLang(OwlConfig.translationProvider);
    }

    public static class TranslatorContext {
        private final String UID;
        private final Object translateObject;

        public TranslatorContext(String ID, String messageObject) {
            UID = ID;
            translateObject = messageObject;
        }

        public TranslatorContext(MessageObject messageObject) {
            UID = messageObject.getChatId()+"_"+messageObject.getId();
            BaseTranslator.AdditionalObjectTranslation additionalObjectTranslation = new BaseTranslator.AdditionalObjectTranslation();
            additionalObjectTranslation.translation = messageObject.type == MessageObject.TYPE_POLL ? ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll : messageObject.messageOwner.message;
            messageObject.originalMessage = additionalObjectTranslation.translation;
            if (messageObject.messageOwner.reply_markup != null && messageObject.messageOwner.reply_markup.rows.size() > 0) {
                messageObject.originalReplyMarkupRows = new MessageHelper.ReplyMarkupButtonsTexts(messageObject.messageOwner.reply_markup.rows);
                additionalObjectTranslation.additionalInfo = new MessageHelper.ReplyMarkupButtonsTexts(messageObject.messageOwner.reply_markup.rows);
            }
            if(messageObject.messageOwner.entities != null && additionalObjectTranslation.translation instanceof String && isSupportedProvider()){
                messageObject.originalEntities = messageObject.messageOwner.entities;
                if (isSupportHTMLMode() && OwlConfig.keepTranslationMarkdown) {
                    additionalObjectTranslation.translation = HTMLKeeper.entitiesToHtml((String) additionalObjectTranslation.translation, messageObject.originalEntities, false);
                }
            }
            translateObject = additionalObjectTranslation;
        }
    }

    public interface TranslateCallback {
        void onTranslate(BaseTranslator.Result result);
        void onPreTranslate();
        void onError(Exception error);
    }
}
