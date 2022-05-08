package it.owlgram.android.translator;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.helpers.EntitiesHelper;
import it.owlgram.android.helpers.MessageHelper;

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

    public static MessageObject applyTranslatedMessage(BaseTranslator.Result result, MessageObject messageObject, long dialog_id, BaseFragment fragment) {
        if (result.translation instanceof String) {
            if(messageObject.originalEntities != null && result.sourceLanguage != null){
                String src_lang = result.sourceLanguage.toUpperCase();
                String language = Translator.getTranslator(OwlConfig.translationProvider).getCurrentTargetLanguage().toUpperCase();
                String top_html = "<b>" + LocaleController.formatString("TranslatorLanguageCode", R.string.TranslatorLanguageCode, src_lang + " → " + language) +"</b>\n\n";
                String result_translation = top_html + result.translation;
                EntitiesHelper.TextWithMention entitiesResult = EntitiesHelper.getEntities(result_translation, messageObject.originalEntities, !isSupportHTMLMode());
                messageObject.messageOwner.message = entitiesResult.text;
                messageObject.messageOwner.entities = entitiesResult.entities;
                if (result.additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                    ((MessageHelper.ReplyMarkupButtonsTexts) result.additionalInfo).applyTextToKeyboard(messageObject.messageOwner.reply_markup.rows);
                }
                if (fragment == null) {
                    messageObject.translated = true;
                    messageObject.translating = false;
                    messageObject.caption = null;
                    messageObject.generateCaption();
                }
            }
        } else if (result.translation instanceof TLRPC.TL_poll) {
            ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll = (TLRPC.TL_poll) result.translation;
        }
        if (fragment != null) {
            fragment.getMessageHelper().resetMessageContent(dialog_id, messageObject, true);
        }
        return messageObject;
    }

    public static MessageObject applyTranslatedMessage(BaseTranslator.Result result, MessageObject messageObject) {
        return applyTranslatedMessage(result, messageObject, 0, null);
    }

    public static MessageObject resetTranslatedMessage(long dialog_id, BaseFragment fragment, MessageObject messageObject) {
        if (messageObject.originalMessage instanceof String) {
            messageObject.messageOwner.message = (String) messageObject.originalMessage;
            messageObject.messageText = messageObject.messageOwner.message;
            if(messageObject.originalEntities != null){
                messageObject.messageOwner.entities = new ArrayList<>(messageObject.originalEntities);
            }
            if(messageObject.originalReplyMarkupRows != null){
                for (int i = 0; i < messageObject.messageOwner.reply_markup.rows.size(); i++) {
                    ArrayList<TLRPC.KeyboardButton> buttonsRow = new ArrayList<>(messageObject.messageOwner.reply_markup.rows.get(i).buttons);
                    for (int j = 0; j < buttonsRow.size(); j++) {
                        Log.e("Translator", "resetTranslatedMessage: " + buttonsRow.get(j).text);
                    }
                }
                ((MessageHelper.ReplyMarkupButtonsTexts) messageObject.originalReplyMarkupRows).applyTextToKeyboard(messageObject.messageOwner.reply_markup.rows);
            }
        } else if (messageObject.originalMessage instanceof TLRPC.TL_poll) {
            ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll = (TLRPC.TL_poll) messageObject.originalMessage;
        }
        return fragment.getMessageHelper().resetMessageContent(dialog_id, messageObject, false);
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

    private static boolean isSupportHTMLMode() {
        return OwlConfig.translationProvider == Translator.PROVIDER_GOOGLE;
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
                if (isSupportHTMLMode()) {
                    additionalObjectTranslation.translation = EntitiesHelper.entitiesToHtml((String) additionalObjectTranslation.translation, messageObject.originalEntities, false);
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
