package it.owlgram.android.helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.Translator;

public class TranslatorActionMessage {
    private static final ArrayList<String> translatingMessage = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void translateMessage(long dialog_id, MessageObject messageObject, BaseFragment fragment, Theme.ResourcesProvider themeDelegate) {
        if (messageObject == null) {
            return;
        }
        String currentID = messageObject.getChatId()+"_"+messageObject.getId();
        if(checkAlreadyTranslating(currentID)) return;
        Object original = messageObject.type == MessageObject.TYPE_POLL ? ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll : messageObject.messageOwner.message;
        messageObject.originalMessage = original;
        boolean supportHTMLMode = OwlConfig.translationProvider == Translator.PROVIDER_GOOGLE;
        boolean isSupportedProvider = supportHTMLMode || Translator.isSupportedOutputLang(OwlConfig.translationProvider);
        if(messageObject.messageOwner.entities != null && original instanceof String && isSupportedProvider){
            messageObject.originalEntities = messageObject.messageOwner.entities;
            if (supportHTMLMode) {
                original = EntitiesHelper.entitiesToHtml((String) original, messageObject.originalEntities, false);
            }
        }
        final MessageObject finalMessageObject = messageObject;
        Object finalOriginal = original;
        translatingMessage.add(currentID);
        Translator.translate(original, new Translator.TranslateCallBack() {
            @Override
            public void onSuccess(BaseTranslator.Result result) {
                translatingMessage.remove(currentID);
                if (result.translation instanceof String) {
                    if(finalMessageObject.originalEntities != null && result.sourceLanguage != null){
                        String src_lang = result.sourceLanguage.toUpperCase();
                        String language = Translator.getTranslator(OwlConfig.translationProvider).getCurrentTargetLanguage().toUpperCase();
                        String top_html = "<b>" + LocaleController.formatString("TranslatorLanguageCode", R.string.TranslatorLanguageCode, src_lang + " → " + language) +"</b>\n\n";
                        String result_translation = top_html + result.translation;
                        EntitiesHelper.TextWithMention entitiesResult = EntitiesHelper.getEntities(result_translation, finalMessageObject.originalEntities, !supportHTMLMode);
                        finalMessageObject.messageOwner.message = entitiesResult.text;
                        finalMessageObject.messageOwner.entities = entitiesResult.entities;
                    } else if(finalOriginal instanceof String) {
                        finalMessageObject.messageOwner.message = finalOriginal + "\n--------\n" + result.translation;
                    }
                } else if (result.translation instanceof TLRPC.TL_poll) {
                    ((TLRPC.TL_messageMediaPoll) finalMessageObject.messageOwner.media).poll = (TLRPC.TL_poll) result.translation;
                }
                fragment.getMessageHelper().resetMessageContent(dialog_id, finalMessageObject, true);
            }

            @Override
            public void onError(Exception e) {
                translatingMessage.remove(currentID);
                Translator.handleTranslationError(fragment.getParentActivity(), e, () -> translateMessage(dialog_id, finalMessageObject, fragment, themeDelegate), themeDelegate);
            }
        });
    }

    public static boolean checkAlreadyTranslating(String currentID) {
        for(int i=0;i<translatingMessage.size();i++){
            String check = translatingMessage.get(i);
            if(check.equals(currentID)){
                return true;
            }
        }
        return false;
    }

    public static void resetTranslateMessage(long dialog_id, BaseFragment fragment, MessageObject messageObject) {
        if (messageObject.originalMessage instanceof String) {
            messageObject.messageOwner.message = (String) messageObject.originalMessage;
            messageObject.messageText = messageObject.messageOwner.message;
            if(messageObject.originalEntities != null){
                messageObject.messageOwner.entities = new ArrayList<>(messageObject.originalEntities);
            }
        } else if (messageObject.originalMessage instanceof TLRPC.TL_poll) {
            ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll = (TLRPC.TL_poll) messageObject.originalMessage;
        }
        fragment.getMessageHelper().resetMessageContent(dialog_id, messageObject, false);
    }
}
