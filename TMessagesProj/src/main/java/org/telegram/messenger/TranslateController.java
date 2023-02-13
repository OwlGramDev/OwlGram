package org.telegram.messenger;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.Collator;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.RestrictedLanguagesSelectActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.entities.HTMLKeeper;
import it.owlgram.android.helpers.MessageHelper;
import it.owlgram.android.settings.DoNotTranslateSettings;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.Translator;
import it.owlgram.android.translator.TranslatorHelper;

public class TranslateController extends BaseController {

    public static final String UNKNOWN_LANGUAGE = "und";

    private static final int REQUIRED_TOTAL_MESSAGES_CHECKED = 8;
    private static final float REQUIRED_PERCENTAGE_MESSAGES_TRANSLATABLE = .60F;
    private static final float REQUIRED_MIN_PERCENTAGE_MESSAGES_UNKNOWN = .65F;

    private static final int MAX_SYMBOLS_PER_REQUEST = 25000;
    public static final int MAX_MESSAGES_PER_REQUEST = 20;
    private static final int GROUPING_TRANSLATIONS_TIMEOUT = 200;

    private final HashMap<Long, Set<Integer>> hideTranslations = new HashMap<>();
    private final HashMap<Long, Set<Integer>> manualTranslations = new HashMap<>();
    private final Set<Long> translatingDialogs = new HashSet<>();
    private final Set<Long> translatableDialogs = new HashSet<>();
    private final HashMap<Long, TranslatableDecision> translatableDialogMessages = new HashMap<>();
    private final HashMap<Long, String> translateDialogLanguage = new HashMap<>();
    private final HashMap<Long, String> detectedDialogLanguage = new HashMap<>();
    private final HashMap<Long, HashMap<Integer, MessageObject>> keptReplyMessageObjects = new HashMap<>();
    private final Set<Long> hideTranslateDialogs = new HashSet<>();

    class TranslatableDecision {
        Set<Integer> certainlyTranslatable = new HashSet<>();
        Set<Integer> unknown = new HashSet<>();
        Set<Integer> certainlyNotTranslatable = new HashSet<>();
    }

    private MessagesController messagesController;

    public TranslateController(MessagesController messagesController) {
        super(messagesController.currentAccount);
        this.messagesController = messagesController;

        AndroidUtilities.runOnUIThread(this::loadTranslatingDialogsCached, 150);
    }

    public boolean isFeatureAvailable() {
        return /*UserConfig.getInstance(currentAccount).isPremium() &&*/ isChatTranslateEnabled();
    }

    public boolean isChatTranslateEnabled() {
        //return MessagesController.getMainSettings(currentAccount).getBoolean("translate_chat_button", true);
        return true;
    }

    public boolean isContextTranslateEnabled() {
        //return MessagesController.getMainSettings(currentAccount).getBoolean("translate_button", MessagesController.getGlobalMainSettings().getBoolean("translate_button", false));
        return false;
    }

    public void setContextTranslateEnabled(boolean enable) {
        MessagesController.getMainSettings(currentAccount).edit().putBoolean("translate_button", enable).apply();
    }

    public static boolean isTranslatable(MessageObject messageObject) {
        return isTranslatable(messageObject, false);
    }

    public static boolean isTranslatable(MessageObject messageObject, boolean forceOwner) {
        return (
                messageObject != null && messageObject.messageOwner != null &&
                        (!messageObject.isOutOwner() || forceOwner) &&
                        (!TextUtils.isEmpty(messageObject.messageOwner.message) ||
                        messageObject.type == MessageObject.TYPE_POLL)
        );
    }

    public boolean isHiddenTranslation(MessageObject messageObject) {
        return hideTranslations.containsKey(messageObject.getDialogId()) && Objects.requireNonNull(hideTranslations.get(messageObject.getDialogId())).contains(messageObject.getId());
    }

    public void hideTranslation(MessageObject messageObject) {
        if (!hideTranslations.containsKey(messageObject.getDialogId())) {
            hideTranslations.put(messageObject.getDialogId(), new HashSet<>());
        }
        Objects.requireNonNull(hideTranslations.get(messageObject.getDialogId())).add(messageObject.getId());
    }

    public void unHideTranslation(MessageObject messageObject) {
        if (hideTranslations.containsKey(messageObject.getDialogId())) {
            Objects.requireNonNull(hideTranslations.get(messageObject.getDialogId())).remove(messageObject.getId());
            if (Objects.requireNonNull(hideTranslations.get(messageObject.getDialogId())).isEmpty()) {
                hideTranslations.remove(messageObject.getDialogId());
            }
        }
    }
    public boolean isManualTranslation(MessageObject messageObject) {
        return manualTranslations.containsKey(messageObject.getDialogId()) && Objects.requireNonNull(manualTranslations.get(messageObject.getDialogId())).contains(messageObject.getId());
    }
    public void addManualTranslation(MessageObject messageObject) {
        if (!manualTranslations.containsKey(messageObject.getDialogId())) {
            manualTranslations.put(messageObject.getDialogId(), new HashSet<>());
        }
        Objects.requireNonNull(manualTranslations.get(messageObject.getDialogId())).add(messageObject.getId());
    }

    public boolean isDialogTranslatable(long dialogId) {
        return (
            isFeatureAvailable() &&
            getUserConfig().getClientUserId() != dialogId &&
            /* DialogObject.isChatDialog(dialogId) &&*/
            translatableDialogs.contains(dialogId)
        );
    }

    public boolean isTranslateDialogHidden(long dialogId) {
        if (hideTranslateDialogs.contains(dialogId)) {
            return true;
        }
        TLRPC.ChatFull chatFull = getMessagesController().getChatFull(-dialogId);
        if (chatFull != null) {
            return chatFull.translations_disabled;
        }
        TLRPC.UserFull userFull = getMessagesController().getUserFull(dialogId);
        if (userFull != null) {
            return userFull.translations_disabled;
        }
        return false;
    }

    public boolean isTranslatingDialog(long dialogId) {
        return isFeatureAvailable() && translatingDialogs.contains(dialogId);
    }

    public boolean isGeneralTranslating(MessageObject messageObject) {
        return isManualTranslation(messageObject) || isTranslatingDialog(messageObject.getDialogId());
    }

    public void toggleTranslatingDialog(long dialogId) {
        toggleTranslatingDialog(dialogId, !isTranslatingDialog(dialogId));
    }

    public boolean toggleTranslatingDialog(long dialogId, boolean value) {
        boolean currentValue = isTranslatingDialog(dialogId), notified = false;
        if (value && !currentValue) {
            translatingDialogs.add(dialogId);
            hideTranslations.remove(dialogId);
            manualTranslations.remove(dialogId);
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, true);
            notified = true;
        } else if (!value && currentValue) {
            translatingDialogs.remove((Long) dialogId);
            manualTranslations.remove(dialogId);
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, false);
            cancelTranslations(dialogId);
            notified = true;
        }
        saveTranslatingDialogsCache();
        return notified;
    }

    private int hash(MessageObject messageObject) {
        if (messageObject == null) {
            return 0;
        }
        return Objects.hash(messageObject.getDialogId(), messageObject.getId());
    }

    private String currentLanguage() {
        String lang = LocaleController.getInstance().getCurrentLocaleInfo().pluralLangCode;
        if (lang != null) {
            lang = lang.split("_")[0];
        }
        return lang;
    }

    public String getDialogTranslateTo(long dialogId) {
        /*String lang = translateDialogLanguage.get(dialogId);
        if (lang == null) {
            lang = TranslateAlert2.getToLanguage();
            if (lang == null || lang.equals(getDialogDetectedLanguage(dialogId))) {
                lang = currentLanguage();
            }
        }
        if ("nb".equals(lang)) {
            lang = "no";
        }
        return lang;*/
        return Translator.getTranslator(OwlConfig.translationProvider).getCurrentTargetLanguage();
    }

    public void setDialogTranslateTo(long dialogId, String language) {
        if (TextUtils.equals(getDialogTranslateTo(dialogId), language)) {
            return;
        }

        boolean wasTranslating = isTranslatingDialog(dialogId);

        if (wasTranslating) {
            AndroidUtilities.runOnUIThread(() -> {
                synchronized (TranslateController.this) {
                    translateDialogLanguage.put(dialogId, language);
                    translatingDialogs.add(dialogId);
                    saveTranslatingDialogsCache();
                }
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, true);
            }, 150);
        } else {
            synchronized (TranslateController.this) {
                translateDialogLanguage.put(dialogId, language);
            }
        }

        cancelTranslations(dialogId);
        synchronized (this) {
            translatingDialogs.remove(dialogId);
        }
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, false);

        TranslateAlert2.setToLanguage(language);
    }

    public void updateDialogFull(long dialogId) {
        if (!isFeatureAvailable() || !isDialogTranslatable(dialogId)) {
            return;
        }

        final boolean wasHidden = hideTranslateDialogs.contains(dialogId);

        boolean hidden = false;
        TLRPC.ChatFull chatFull = getMessagesController().getChatFull(-dialogId);
        if (chatFull != null) {
            hidden = chatFull.translations_disabled;
        } else {
            TLRPC.UserFull userFull = getMessagesController().getUserFull(dialogId);
            if (userFull != null) {
                hidden = userFull.translations_disabled;
            }
        }

        synchronized (this) {
            if (hidden) {
                hideTranslateDialogs.add(dialogId);
            } else {
                hideTranslateDialogs.remove(dialogId);
            }
        }

        if (wasHidden != hidden) {
            saveTranslatingDialogsCache();
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, isTranslatingDialog(dialogId));
        }
    }

    public void setHideTranslateDialog(long dialogId, boolean hide) {
        setHideTranslateDialog(dialogId, hide, false);
    }

    public void setHideTranslateDialog(long dialogId, boolean hide, boolean doNotNotify) {
        TLRPC.TL_messages_togglePeerTranslations req = new TLRPC.TL_messages_togglePeerTranslations();
        req.peer = getMessagesController().getInputPeer(dialogId);
        req.disabled = hide;
        getConnectionsManager().sendRequest(req, null);

        TLRPC.ChatFull chatFull = getMessagesController().getChatFull(-dialogId);
        if (chatFull != null) {
            chatFull.translations_disabled = hide;
            getMessagesStorage().updateChatInfo(chatFull, true);
        }
        TLRPC.UserFull userFull = getMessagesController().getUserFull(dialogId);
        if (userFull != null) {
            userFull.translations_disabled = hide;
            getMessagesStorage().updateUserInfo(userFull, true);
        }

        synchronized (this) {
            if (hide) {
                hideTranslateDialogs.add(dialogId);
            } else {
                hideTranslateDialogs.remove(dialogId);
            }
        }
        saveTranslatingDialogsCache();

        if (!doNotNotify) {
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, isTranslatingDialog(dialogId));
        }
    }

    private static List<String> languagesOrder = Arrays.asList(
        "en", "ar", "zh", "fr", "de", "it", "ja", "ko", "pt", "ru", "es", "uk"
    );

    private static List<String> allLanguages = Arrays.asList(
        "af", "sq", "am", "ar", "hy", "az", "eu", "be", "bn", "bs", "bg", "ca", "ceb", "zh", "co", "hr", "cs", "da", "nl", "en", "eo", "et", "fi", "fr", "fy", "gl", "ka", "de", "el", "gu", "ht", "ha", "haw", "he", "iw", "hi", "hmn", "hu", "is", "ig", "id", "ga", "it", "ja", "jv", "kn", "kk", "km", "rw", "ko", "ku", "ky", "lo", "la", "lv", "lt", "lb", "mk", "mg", "ms", "ml", "mt", "mi", "mr", "mn", "my", "ne", "no", "ny", "or", "ps", "fa", "pl", "pt", "pa", "ro", "ru", "sm", "gd", "sr", "st", "sn", "sd", "si", "sk", "sl", "so", "es", "su", "sw", "sv", "tl", "tg", "ta", "tt", "te", "th", "tr", "tk", "uk", "ur", "ug", "uz", "vi", "cy", "xh", "yi", "yo", "zu"
    );

    public static class Language {
        public String code;
        public String displayName;
    }

    public static ArrayList<Language> getLanguages() {
        ArrayList<Language> result = new ArrayList<>();
        for (int i = 0; i < allLanguages.size(); ++i) {
            Language language = new Language();
            language.code = allLanguages.get(i);
            language.displayName = TranslateAlert2.capitalFirst(TranslateAlert2.languageName(language.code));
            if (language.displayName == null) {
                continue;
            }
            result.add(language);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Collator collator = Collator.getInstance(Locale.getDefault());
            Collections.sort(result, (lng1, lng2) -> collator.compare(lng1.displayName, lng2.displayName));
        } else {
            Collections.sort(result, Comparator.comparing(lng -> lng.displayName));
        }
        return result;
    }

    private static LinkedHashSet<String> suggestedLanguageCodes = null;
    public static void invalidateSuggestedLanguageCodes() {
        suggestedLanguageCodes = null;
    }
    public static void analyzeSuggestedLanguageCodes() {
        LinkedHashSet<String> langs = new LinkedHashSet<>();
        try {
            langs.addAll(DoNotTranslateSettings.getRestrictedLanguages());
        } catch (Exception e3) {
            FileLog.e(e3);
        }
        try {
            InputMethodManager imm = (InputMethodManager) ApplicationLoader.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> ims = imm.getEnabledInputMethodList();
            for (InputMethodInfo method : ims) {
                List<InputMethodSubtype> submethods = imm.getEnabledInputMethodSubtypeList(method, true);
                for (InputMethodSubtype submethod : submethods) {
                    if ("keyboard".equals(submethod.getMode())) {
                        String currentLocale = submethod.getLocale();
                        if (currentLocale != null && currentLocale.contains("_")) {
                            currentLocale = currentLocale.split("_")[0];
                        }
                        if (TranslateAlert2.languageName(currentLocale) != null) {
                            langs.add(currentLocale.split("-")[0]);
                        }
                    }
                }
            }
        } catch (Exception e4) {
            FileLog.e(e4);
        }
        suggestedLanguageCodes = langs;
    }

    public static ArrayList<Language> getSuggestedLanguages(String except) {
        ArrayList<Language> result = new ArrayList<>();
        if (suggestedLanguageCodes == null) {
            analyzeSuggestedLanguageCodes();
            if (suggestedLanguageCodes == null) {
                return result;
            }
        }
        Iterator<String> i = suggestedLanguageCodes.iterator();
        while (i.hasNext()) {
            final String code = i.next();
            if (TextUtils.equals(code, except) || "no".equals(except) && "nb".equals(code) || "nb".equals(except) && "no".equals(code)) {
                continue;
            }
            Language language = new Language();
            language.code = code;
            language.displayName = TranslateAlert2.capitalFirst(TranslateAlert2.languageName(language.code));
            if (language.displayName == null) {
                continue;
            }
            result.add(language);
        }
        return result;
    }

    public static ArrayList<LocaleController.LocaleInfo> getLocales() {
        HashMap<String, LocaleController.LocaleInfo> languages = LocaleController.getInstance().languagesDict;
        ArrayList<LocaleController.LocaleInfo> locales = new ArrayList<>(languages.values());
        for (int i = 0; i < locales.size(); ++i) {
            LocaleController.LocaleInfo locale = locales.get(i);
            if (locale == null || locale.shortName != null && locale.shortName.endsWith("_raw") || !"remote".equals(locale.pathToFile)) {
                locales.remove(i);
                i--;
            }
        }

        final LocaleController.LocaleInfo currentLocale = LocaleController.getInstance().getCurrentLocaleInfo();
        Comparator<LocaleController.LocaleInfo> comparator = (o, o2) -> {
            if (o == currentLocale) {
                return -1;
            } else if (o2 == currentLocale) {
                return 1;
            }
            final int index1 = languagesOrder.indexOf(o.pluralLangCode);
            final int index2 = languagesOrder.indexOf(o2.pluralLangCode);
            if (index1 >= 0 && index2 >= 0) {
                return index1 - index2;
            } else if (index1 >= 0) {
                return -1;
            } else if (index2 >= 0) {
                return 1;
            }
            if (o.serverIndex == o2.serverIndex) {
                return o.name.compareTo(o2.name);
            }
            if (o.serverIndex > o2.serverIndex) {
                return 1;
            } else if (o.serverIndex < o2.serverIndex) {
                return -1;
            }
            return 0;
        };
        Collections.sort(locales, comparator);

        return locales;
    }

    public void checkRestrictedLanguagesUpdate() {
        synchronized (this) {
            translatableDialogMessages.clear();

            ArrayList<Long> toNotify = new ArrayList<>();
            HashSet<String> languages = DoNotTranslateSettings.getRestrictedLanguages();
            for (long dialogId : translatableDialogs) {
                String language = detectedDialogLanguage.get(dialogId);
                if (language != null && languages.contains(language.split("-")[0])) {
                    cancelTranslations(dialogId);
                    translatingDialogs.remove(dialogId);
                    toNotify.add(dialogId);
                }
            }
            translatableDialogs.clear();
            saveTranslatingDialogsCache();

            for (long dialogId : toNotify) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogTranslate, dialogId, false);
            }
        }
    }

    @Nullable
    public String getDialogDetectedLanguage(long dialogId) {
        return detectedDialogLanguage.get(dialogId);
    }

    public void checkTranslation(MessageObject messageObject, boolean onScreen) {
        checkTranslation(messageObject, onScreen, false);
    }

    public void applyTranslationResult(MessageObject messageObject, BaseTranslator.Result result) {
        messageObject.messageOwner.originalLanguage = result.sourceLanguage;
        messageObject.messageOwner.translatedToLanguage = getDialogTranslateTo(messageObject.getDialogId());
        if (result.translation instanceof String || result.translation instanceof TLRPC.TL_textWithEntities) {
            TLRPC.TL_textWithEntities textWithEntities;
            if (result.translation instanceof String) {
                Pair<String, ArrayList<TLRPC.MessageEntity>> entitiesResult = HTMLKeeper.htmlToEntities((String) result.translation, messageObject.messageOwner.entities, !TranslatorHelper.isSupportHTMLMode());
                textWithEntities = new TLRPC.TL_textWithEntities();
                textWithEntities.text = entitiesResult.first;
                textWithEntities.entities = entitiesResult.second;
                TLRPC.TL_textWithEntities originalTextWithEntities = new TLRPC.TL_textWithEntities();
                originalTextWithEntities.text = messageObject.messageOwner.message;
                originalTextWithEntities.entities = messageObject.messageOwner.entities;
                textWithEntities = TranslateAlert2.preprocess(originalTextWithEntities, textWithEntities);
            } else {
                textWithEntities = (TLRPC.TL_textWithEntities) result.translation;
            }
            messageObject.messageOwner.translatedText = textWithEntities;
            if (result.additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                messageObject.messageOwner.translatedReplyMarkupRows = (MessageHelper.ReplyMarkupButtonsTexts) result.additionalInfo;
            }
        } else if (result.translation instanceof MessageHelper.PollTexts) {
            messageObject.messageOwner.translatedPoll = (MessageHelper.PollTexts) result.translation;
        }
    }

    private void checkTranslation(MessageObject messageObject, boolean onScreen, boolean keepReply) {
        if (!isFeatureAvailable()) {
            return;
        }
        if (messageObject == null || messageObject.messageOwner == null) {
            return;
        }

        long dialogId = messageObject.getDialogId();

        if (!keepReply && messageObject.replyMessageObject != null) {
            checkTranslation(messageObject.replyMessageObject, onScreen, true);
        }

        if (!isTranslatable(messageObject)) {
            return;
        }

        if (!isTranslatingDialog(dialogId)) {
            checkLanguage(messageObject);
            return;
        }

        final String language = getDialogTranslateTo(dialogId);
        MessageObject potentialReplyMessageObject;
        if (!keepReply && (messageObject.messageOwner.translatedText == null || !language.equals(messageObject.messageOwner.translatedToLanguage)) && (potentialReplyMessageObject = findReplyMessageObject(dialogId, messageObject.getId())) != null) {
            messageObject.messageOwner.translatedToLanguage = potentialReplyMessageObject.messageOwner.translatedToLanguage;
            messageObject.messageOwner.originalLanguage = potentialReplyMessageObject.messageOwner.originalLanguage;
            messageObject.messageOwner.translatedReplyMarkupRows = potentialReplyMessageObject.messageOwner.translatedReplyMarkupRows;
            messageObject.messageOwner.originalReplyMarkupRows = potentialReplyMessageObject.messageOwner.originalReplyMarkupRows;
            messageObject.messageOwner.translatedText = potentialReplyMessageObject.messageOwner.translatedText;
            messageObject.messageOwner.translatedPoll = potentialReplyMessageObject.messageOwner.translatedPoll;
            messageObject.messageOwner.originalPoll = potentialReplyMessageObject.messageOwner.originalPoll;
            messageObject = potentialReplyMessageObject;
        }

        if (onScreen && isTranslatingDialog(dialogId)) {
            final MessageObject finalMessageObject = messageObject;
            if (finalMessageObject.messageOwner.translatedText == null || !language.equals(finalMessageObject.messageOwner.translatedToLanguage)) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslating, finalMessageObject);
                pushToTranslate(finalMessageObject, result -> {
                    applyTranslationResult(finalMessageObject, result);
                    if (keepReply) {
                        keepReplyMessage(finalMessageObject);
                    }

                    getMessagesStorage().updateMessageCustomParams(dialogId, finalMessageObject.messageOwner);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, finalMessageObject);

                    ArrayList<MessageObject> dialogMessages = messagesController.dialogMessage.get(dialogId);
                    if (dialogMessages != null) {
                        for (int i = 0; i < dialogMessages.size(); ++i) {
                            MessageObject dialogMessage = dialogMessages.get(i);
                            if (dialogMessage != null && dialogMessage.getId() == finalMessageObject.getId()) {
                                dialogMessage.messageOwner.originalLanguage = finalMessageObject.messageOwner.originalLanguage;
                                dialogMessage.messageOwner.translatedToLanguage = finalMessageObject.messageOwner.translatedToLanguage;
                                dialogMessage.messageOwner.translatedText = finalMessageObject.messageOwner.translatedText;
                                dialogMessage.messageOwner.translatedReplyMarkupRows = finalMessageObject.messageOwner.translatedReplyMarkupRows;
                                dialogMessage.messageOwner.originalReplyMarkupRows = finalMessageObject.messageOwner.originalReplyMarkupRows;
                                dialogMessage.messageOwner.translatedPoll = finalMessageObject.messageOwner.translatedPoll;
                                dialogMessage.messageOwner.originalPoll = finalMessageObject.messageOwner.originalPoll;
                                if (dialogMessage.updateTranslation()) {
                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, 0);
                                }
                                break;
                            }
                        }
                    }
                });
            } else if (keepReply) {
                keepReplyMessage(messageObject);
            }
        }
    }

    public void invalidateTranslation(MessageObject messageObject) {
        if (!isFeatureAvailable()) {
            return;
        }
        if (messageObject == null || messageObject.messageOwner == null) {
            return;
        }
        final long dialogId = messageObject.getDialogId();
        messageObject.messageOwner.translatedToLanguage = null;
        messageObject.messageOwner.translatedText = null;
        messageObject.messageOwner.translatedReplyMarkupRows = null;
        messageObject.messageOwner.translatedPoll = null;
        getMessagesStorage().updateMessageCustomParams(dialogId, messageObject.messageOwner);
        AndroidUtilities.runOnUIThread(() -> {
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, messageObject, isTranslatingDialog(dialogId));
        });
    }

    public void checkDialogMessages(long dialogId) {
        if (!isFeatureAvailable()) {
            return;
        }
        getMessagesStorage().getStorageQueue().postRunnable(() -> {
            final ArrayList<MessageObject> dialogMessages = messagesController.dialogMessage.get(dialogId);
            if (dialogMessages == null) {
                return;
            }
            ArrayList<TLRPC.Message> customProps = new ArrayList<>();
            for (int i = 0; i < dialogMessages.size(); ++i) {
                MessageObject dialogMessage = dialogMessages.get(i);
                if (dialogMessage == null || dialogMessage.messageOwner == null) {
                    customProps.add(null);
                    continue;
                }
                customProps.add(getMessagesStorage().getMessageWithCustomParamsOnlyInternal(dialogMessage.getId(), dialogMessage.getDialogId()));
            }
            AndroidUtilities.runOnUIThread(() -> {
                boolean updated = false;
                for (int i = 0; i < Math.min(customProps.size(), dialogMessages.size()); ++i) {
                    MessageObject dialogMessage = dialogMessages.get(i);
                    TLRPC.Message props = customProps.get(i);
                    if (dialogMessage == null || dialogMessage.messageOwner == null || props == null) {
                        continue;
                    }
                    dialogMessage.messageOwner.translatedText = props.translatedText;
                    dialogMessage.messageOwner.translatedToLanguage = props.translatedToLanguage;
                    dialogMessage.messageOwner.originalLanguage = props.originalLanguage;
                    if (dialogMessage.updateTranslation(false)) {
                        updated = true;
                    }
                }
                if (updated) {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, 0);
                }
            });
        });
    }


    public void cleanup() {
        cancelAllTranslations();
        resetTranslatingDialogsCache();

        translatingDialogs.clear();
        translatableDialogs.clear();
        hideTranslations.clear();
        manualTranslations.clear();
        translatableDialogMessages.clear();
        translateDialogLanguage.clear();
        detectedDialogLanguage.clear();
        keptReplyMessageObjects.clear();
        hideTranslateDialogs.clear();
        loadingTranslations.clear();
    }

    private ArrayList<Integer> pendingLanguageChecks = new ArrayList<>();
    private void checkLanguage(MessageObject messageObject) {
        if (!LanguageDetector.hasSupport()) {
            return;
        }
        if (!isTranslatable(messageObject) || messageObject.messageOwner == null || TextUtils.isEmpty(messageObject.messageOwner.message)) {
            return;
        }

        final long dialogId = messageObject.getDialogId();
        final int hash = hash(messageObject);
        if (isDialogTranslatable(dialogId)) {
            return;
        }
        if (pendingLanguageChecks.contains(hash)) {
            return;
        }

        checkDialogTranslatable(messageObject);
    }

    private void checkDialogTranslatable(MessageObject messageObject) {
        if (messageObject == null || messageObject.messageOwner == null) {
            return;
        }

        final long dialogId = messageObject.getDialogId();
        TranslatableDecision translatableMessages = translatableDialogMessages.get(dialogId);
        if (translatableMessages == null) {
            translatableDialogMessages.put(dialogId, translatableMessages = new TranslatableDecision());
        }

        final boolean isUnknown = isTranslatable(messageObject) && (
            messageObject.messageOwner.originalLanguage == null ||
            UNKNOWN_LANGUAGE.equals(messageObject.messageOwner.originalLanguage)
        );
        final boolean translatable = (
            isTranslatable(messageObject) &&
            messageObject.messageOwner.originalLanguage != null &&
            !DoNotTranslateSettings.getRestrictedLanguages().contains(messageObject.messageOwner.originalLanguage.split("-")[0])
        );

        if (isUnknown) {
            translatableMessages.unknown.add(messageObject.getId());
        } else {
            (translatable ? translatableMessages.certainlyTranslatable : translatableMessages.certainlyNotTranslatable).add(messageObject.getId());
        }

        if (!isUnknown) {
            detectedDialogLanguage.put(dialogId, messageObject.messageOwner.originalLanguage);
        }

        final int translatableCount = translatableMessages.certainlyTranslatable.size();
        final int unknownCount = translatableMessages.unknown.size();
        final int notTranslatableCount = translatableMessages.certainlyNotTranslatable.size();
        final int totalCount = translatableCount + unknownCount + notTranslatableCount;
        /*if (
            totalCount >= REQUIRED_TOTAL_MESSAGES_CHECKED &&
            (translatableCount / (float) (translatableCount + notTranslatableCount)) >= REQUIRED_PERCENTAGE_MESSAGES_TRANSLATABLE &&
            (unknownCount / (float) totalCount) < REQUIRED_MIN_PERCENTAGE_MESSAGES_UNKNOWN
        ) {*/
            translatableDialogs.add(dialogId);
            translatableDialogMessages.remove((Long) dialogId);
            AndroidUtilities.runOnUIThread(() -> {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogIsTranslatable, dialogId);
            }, 450);
        //}
    }

    public void addTranslatingMessage(MessageObject messageObject) {
        loadingTranslations.add(messageObject.getId());
    }

    public void removeTranslatingMessage(MessageObject messageObject) {
        loadingTranslations.remove(messageObject.getId());
    }

    private final Set<Integer> loadingTranslations = new HashSet<>();
    private final HashMap<Long, ArrayList<PendingTranslation>> pendingTranslations = new HashMap<>();

    private static class PendingTranslation {
        Runnable runnable;
        ArrayList<Integer> messageIds = new ArrayList<>();
        ArrayList<Object> messagesData = new ArrayList<>();
        ArrayList<Utilities.Callback<BaseTranslator.Result>> callbacks = new ArrayList<>();
        String language;

        int symbolsCount;

        String token;
    }

    private void pushToTranslate(
            MessageObject message,
            Utilities.Callback<BaseTranslator.Result> callback
    ) {
        if (message == null || callback == null) {
            return;
        }
        long dialogId = message.getDialogId();

        PendingTranslation pendingTranslation;
        synchronized (this) {
            ArrayList<PendingTranslation> dialogPendingTranslations = pendingTranslations.get(dialogId);
            if (dialogPendingTranslations == null) {
                pendingTranslations.put(dialogId, dialogPendingTranslations = new ArrayList<>());
            }

            if (dialogPendingTranslations.isEmpty()) {
                dialogPendingTranslations.add(pendingTranslation = new PendingTranslation());
            } else {
                pendingTranslation = dialogPendingTranslations.get(dialogPendingTranslations.size() - 1);
            }

            if (pendingTranslation.messageIds.contains(message.getId())) {
                return;
            }
            TranslatorHelper.TranslatorContext context = new TranslatorHelper.TranslatorContext(message);
            int messageSymbolsCount = context.getSymbolsCount();

            if (pendingTranslation.symbolsCount + messageSymbolsCount >= MAX_SYMBOLS_PER_REQUEST ||
                    pendingTranslation.messageIds.size() + 1 >= MAX_MESSAGES_PER_REQUEST) {
                dialogPendingTranslations.add(pendingTranslation = new PendingTranslation());
            }

            if (pendingTranslation.runnable != null) {
                AndroidUtilities.cancelRunOnUIThread(pendingTranslation.runnable);
            }

            loadingTranslations.add(message.getId());
            pendingTranslation.messageIds.add(message.getId());
            pendingTranslation.callbacks.add(callback);
            pendingTranslation.symbolsCount += messageSymbolsCount;

            pendingTranslation.messagesData.add(context.getTranslateObject());
            final PendingTranslation pendingTranslation1 = pendingTranslation;
            pendingTranslation.runnable = () -> {
                synchronized (TranslateController.this) {
                    ArrayList<PendingTranslation> dialogPendingTranslations1 = pendingTranslations.get(dialogId);
                    if (dialogPendingTranslations1 != null) {
                        dialogPendingTranslations1.remove(pendingTranslation1);
                        if (dialogPendingTranslations1.isEmpty()) {
                            pendingTranslations.remove(dialogId);
                        }
                    }
                }
                String token = Translator.translate(pendingTranslation1.messagesData, (error, results) -> {
                    final ArrayList<Integer> ids;
                    final ArrayList<Utilities.Callback<BaseTranslator.Result>> callbacks;
                    synchronized (TranslateController.this) {
                        ids = pendingTranslation1.messageIds;
                        callbacks = pendingTranslation1.callbacks;
                    }
                    if (results != null) {
                        final int count = Math.min(callbacks.size(), results.size());
                        for (int i = 0; i < count; ++i) {
                            callbacks.get(i).run(results.get(i));
                        }

                    } else {
                        FileLog.e("TranslateController", error);
                        toggleTranslatingDialog(dialogId, false);
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_ERROR, LocaleController.getString("TranslationFailedAlert2", R.string.TranslationFailedAlert2));
                    }

                    synchronized (TranslateController.this) {
                        for (int i = 0; i < ids.size(); ++i) {
                            loadingTranslations.remove(ids.get(i));
                        }
                    }
                });
                synchronized (TranslateController.this) {
                    pendingTranslation1.token = token;
                }
            };
            AndroidUtilities.runOnUIThread(pendingTranslation.runnable, GROUPING_TRANSLATIONS_TIMEOUT);
        }
    }

    public boolean isTranslating(MessageObject messageObject) {
        synchronized (this) {
            return messageObject != null && isGeneralTranslating(messageObject) && loadingTranslations.contains(messageObject.getId());
        }
    }

    public boolean isTranslating(MessageObject messageObject, MessageObject.GroupedMessages group) {
        if (messageObject == null) {
            return false;
        }
        if (!isGeneralTranslating(messageObject)) {
            return false;
        }
        synchronized (this) {
            if (loadingTranslations.contains(messageObject.getId())) {
                return true;
            }
            if (group != null) {
                for (MessageObject message : group.messages) {
                    if (loadingTranslations.contains(message.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void cancelAllTranslations() {
        synchronized (this) {
            for (ArrayList<PendingTranslation> translations : pendingTranslations.values()) {
                if (translations != null) {
                    for (PendingTranslation pendingTranslation : translations) {
                        AndroidUtilities.cancelRunOnUIThread(pendingTranslation.runnable);
                        if (!TextUtils.isEmpty(pendingTranslation.token)) {
                            Translator.getCurrentTranslator().cancelRequest(pendingTranslation.token);
                            for (Integer messageId : pendingTranslation.messageIds) {
                                loadingTranslations.remove(messageId);
                            }
                        }
                    }
                }
            }
        }
    }

    public void cancelTranslations(long dialogId) {
        synchronized (this) {
            ArrayList<PendingTranslation> translations = pendingTranslations.get(dialogId);
            if (translations != null) {
                for (PendingTranslation pendingTranslation : translations) {
                    AndroidUtilities.cancelRunOnUIThread(pendingTranslation.runnable);
                    if (!TextUtils.isEmpty(pendingTranslation.token)) {
                        Translator.getCurrentTranslator().cancelRequest(pendingTranslation.token);
                        for (Integer messageId : pendingTranslation.messageIds) {
                            loadingTranslations.remove(messageId);
                        }
                    }
                }
                pendingTranslations.remove((Long) dialogId);
            }
        }
    }

    private void keepReplyMessage(MessageObject messageObject) {
        if (messageObject == null) {
            return;
        }
        HashMap<Integer, MessageObject> map = keptReplyMessageObjects.get(messageObject.getDialogId());
        if (map == null) {
            keptReplyMessageObjects.put(messageObject.getDialogId(), map = new HashMap<>());
        }
        map.put(messageObject.getId(), messageObject);
    }

    public MessageObject findReplyMessageObject(long dialogId, int messageId) {
        HashMap<Integer, MessageObject> map = keptReplyMessageObjects.get(dialogId);
        if (map == null) {
            return null;
        }
        return map.get(messageId);
    }

    private void clearAllKeptReplyMessages(long dialogId) {
        keptReplyMessageObjects.remove(dialogId);
    }


    private void loadTranslatingDialogsCached() {
        /*if (!isFeatureAvailable()) {
            return;
        }

        String translatingDialogsCache = messagesController.getMainSettings().getString("translating_dialog_languages2", null);
        if (translatingDialogsCache == null) {
            return;
        }
        String[] dialogs = translatingDialogsCache.split(";");

        HashSet<String> restricted = DoNotTranslateSettings.getRestrictedLanguages();
        for (int i = 0; i < dialogs.length; ++i) {
            String[] keyval = dialogs[i].split("=");
            if (keyval.length < 2) {
                continue;
            }
            long did = Long.parseLong(keyval[0]);
            String[] langs = keyval[1].split(">");
            if (langs.length != 2) {
                continue;
            }
            String from = langs[0], to = langs[1];
            if ("null".equals(from)) from = null;
            if ("null".equals(to)) to = null;
            if (from != null) {
                detectedDialogLanguage.put(did, from);
                if (!restricted.contains(from.split("-")[0])) {
                    translatingDialogs.add(did);
                    translatableDialogs.add(did);
                }
                if (to != null) {
                    translateDialogLanguage.put(did, to);
                }
            }
        }

        Set<String> hidden = messagesController.getMainSettings().getStringSet("hidden_translation_at", null);
        if (hidden != null) {
            Iterator<String> i = hidden.iterator();
            while (i.hasNext()) {
                try {
                    hideTranslateDialogs.add(Long.parseLong(i.next()));
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }*/
    }

    private void saveTranslatingDialogsCache() {
        /*StringBuilder langset = new StringBuilder();
        Iterator<Long> i = translatingDialogs.iterator();
        boolean first = true;
        while (i.hasNext()) {
            try {
                long did = i.next();
                if (!first) {
                    langset.append(";");
                }
                if (first) {
                    first = false;
                }
                String lang = detectedDialogLanguage.get(did);
                if (lang == null) {
                    lang = "null";
                }
                String tolang = getDialogTranslateTo(did);
                if (tolang == null) {
                    tolang = "null";
                }
                langset.append(did).append("=").append(lang).append(">").append(tolang);
            } catch (Exception e) {}
        }

        Set<String> hidden = new HashSet<>();
        i = hideTranslateDialogs.iterator();
        while (i.hasNext()) {
            try {
                hidden.add("" + i.next());
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        MessagesController.getMainSettings(currentAccount).edit().putString("translating_dialog_languages2", langset.toString()).putStringSet("hidden_translation_at", hidden).apply();*/
    }

    private void resetTranslatingDialogsCache() {
        MessagesController.getMainSettings(currentAccount).edit().remove("translating_dialog_languages2").remove("hidden_translation_at").apply();
    }
}
