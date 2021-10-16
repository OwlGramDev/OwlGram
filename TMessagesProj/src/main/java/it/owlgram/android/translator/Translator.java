package it.owlgram.android.translator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.helpers.EntitiesHelper;
import it.owlgram.android.helpers.PopupHelper;

public class Translator {

    public static final int PROVIDER_GOOGLE = 1;
    public static final int PROVIDER_YANDEX = 4;
    public static final int PROVIDER_DEEPL = 5;
    public static final int PROVIDER_NIU = 9;

    @SuppressLint("StaticFieldLeak")
    private static AlertDialog progressDialog;

    public static void showTranslateDialog(Context context, String query, Runnable callback) {
        showTranslateDialog(context, query, callback, null);
    }

    public static void showTranslateDialog(Context context, String query, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        try {
            progressDialog.dismiss();
        } catch (Exception ignore) {

        }
        progressDialog = new AlertDialog(context, 3, resourcesProvider);
        progressDialog.showDelayed(400);
        translate(query, new TranslateCallBack() {
            @Override
            public void onSuccess(Object translation) {
                try {
                    progressDialog.dismiss();
                } catch (Exception ignore) {

                }

                TextView messageTextView = new TextView(context);
                messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
                messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                messageTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
                messageTextView.setTextIsSelectable(true);
                messageTextView.setText((String) translation);
                messageTextView.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(4), AndroidUtilities.dp(24), AndroidUtilities.dp(4));

                AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
                builder.setView(messageTextView);
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                builder.setNeutralButton(LocaleController.getString("Copy", R.string.Copy), (dialog, which) -> {
                    AndroidUtilities.addToClipboard((String) translation);
                    if (callback != null) {
                        callback.run();
                    }
                });
                builder.show();
            }

            @Override
            public void onError(Exception e) {
                handleTranslationError(context, e, () -> showTranslateDialog(context, query, callback, resourcesProvider), resourcesProvider);
            }
        });
    }

    public static void handleTranslationError(Context context, final Exception e, final Runnable onRetry, Theme.ResourcesProvider resourcesProvider) {
        if (context == null) {
            return;
        }
        try {
            progressDialog.dismiss();
        } catch (Exception ignore) {

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        if (e instanceof UnsupportedTargetLanguageException) {
            builder.setMessage(LocaleController.getString("OwlgramTranslateApiUnsupported", R.string.OwlgramTranslateApiUnsupported));
            builder.setPositiveButton(LocaleController.getString("OwlgramTranslationProviderShort", R.string.OwlgramTranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, null, resourcesProvider));
        } else {
            if (e != null && e.getLocalizedMessage() != null) {
                builder.setTitle(LocaleController.getString("TranslateFailed", R.string.OwlgramTranslateFailed));
                builder.setMessage(e.getLocalizedMessage());
            } else {
                builder.setMessage(LocaleController.getString("TranslateFailed", R.string.OwlgramTranslateFailed));
            }
            if (onRetry != null) {
                builder.setPositiveButton(LocaleController.getString("Retry", R.string.Retry), (dialog, which) -> onRetry.run());
            }
            builder.setNeutralButton(LocaleController.getString("OwlgramTranslationProviderShort", R.string.OwlgramTranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, null, resourcesProvider));
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show();
    }

    public static Pair<ArrayList<String>, ArrayList<Integer>> getProviders() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        names.add(LocaleController.getString("OwlgramProviderDeepLTranslate", R.string.OwlgramProviderDeepLTranslate));
        types.add(Translator.PROVIDER_DEEPL);
        names.add(LocaleController.getString("OwlgramProviderGoogleTranslate", R.string.OwlgramProviderGoogleTranslate));
        types.add(Translator.PROVIDER_GOOGLE);
        names.add(LocaleController.getString("OwlgramProviderNiuTrans", R.string.OwlgramProviderNiuTrans));
        types.add(Translator.PROVIDER_NIU);
        names.add(LocaleController.getString("OwlgramProviderYandex", R.string.OwlgramProviderYandex));
        types.add(Translator.PROVIDER_YANDEX);
        return new Pair<>(names, types);
    }

    public static void showTranslationTargetSelector(Context context, View view, Runnable callback) {
        showTranslationTargetSelector(context, view, callback, null);
    }

    public static void showTranslationTargetSelector(Context context, View view, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        BaseTranslator translator = Translator.getCurrentTranslator();
        ArrayList<String> targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        ArrayList<CharSequence> names = new ArrayList<>();
        for (String language : targetLanguages) {
            Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Locale.forLanguageTag(language) : new Locale(language);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(locale.getScript())) {
                names.add(HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                names.add(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale))));
            }
        }
        AndroidUtilities.selectionSort(names, targetLanguages);

        targetLanguages.add(0, "app");
        names.add(0, LocaleController.getString("Default", R.string.Default));

        PopupHelper.show(names, LocaleController.getString("OwlgramTranslationTarget", R.string.OwlgramTranslationTarget), targetLanguages.indexOf(OwlConfig.translationTarget), context, view, i -> {
            OwlConfig.setTranslationTarget(targetLanguages.get(i));
            callback.run();
        }, resourcesProvider);
    }

    public static void showTranslationProviderSelector(Context context, View view, MessagesStorage.BooleanCallback callback) {
        showTranslationProviderSelector(context, view, callback, null);
    }

    public static Object[] getEntities(String text, ArrayList<TLRPC.MessageEntity> entities) {
        ArrayList<TLRPC.MessageEntity> returnEntities = new ArrayList<>();
        ArrayList<TLRPC.MessageEntity> copyEntities = new ArrayList<>(entities);
        Pattern p = Pattern.compile("<(.*?)>(.*?)</.*?>");
        Matcher m = p.matcher(text);
        String new_text = text;
        while(m.find()){
            String html = m.group(0);
            String html_tag = m.group(1);
            String content = m.group(2);
            if(html != null && content != null && html_tag != null){
                int offset = new_text.indexOf(html);
                int length = content.length();
                new_text = new_text.replace(html, content);
                String[] html_tags = html_tag.split("-");
                for (String tag: html_tags) {
                    TLRPC.MessageEntity entity = null;
                    switch (tag) {
                        case "b":
                            entity = new TLRPC.TL_messageEntityBold();
                            break;
                        case "i":
                            entity = new TLRPC.TL_messageEntityItalic();
                            break;
                        case "u":
                            entity = new TLRPC.TL_messageEntityUnderline();
                            break;
                        case "s":
                            entity = new TLRPC.TL_messageEntityStrike();
                            break;
                        case "c":
                            entity = new TLRPC.TL_messageEntityCode();
                            break;
                        case "p":
                            entity = new TLRPC.TL_messageEntityPre();
                            break;
                        case "q":
                            entity = new TLRPC.TL_messageEntityBlockquote();
                            break;
                        case "a":
                            for (int i = 0; i < copyEntities.size(); i++) {
                                TLRPC.MessageEntity old_entity = copyEntities.get(i);
                                boolean found = false;
                                if (old_entity instanceof TLRPC.TL_messageEntityMentionName) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityMentionName();
                                    ((TLRPC.TL_messageEntityMentionName) entity).user_id = ((TLRPC.TL_messageEntityMentionName) old_entity).user_id;
                                } else if (old_entity instanceof TLRPC.TL_inputMessageEntityMentionName) {
                                    found = true;
                                    entity = new TLRPC.TL_inputMessageEntityMentionName();
                                    ((TLRPC.TL_inputMessageEntityMentionName) entity).user_id = ((TLRPC.TL_inputMessageEntityMentionName) old_entity).user_id;
                                } else if (old_entity instanceof TLRPC.TL_messageEntityTextUrl) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityTextUrl();
                                    ((TLRPC.TL_messageEntityTextUrl) entity).url = ((TLRPC.TL_messageEntityTextUrl) old_entity).url;
                                } else if (old_entity instanceof TLRPC.TL_messageEntityUrl) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityUrl();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityMention) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityMention();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityBotCommand) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityBotCommand();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityHashtag) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityHashtag();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityCashtag) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityCashtag();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityEmail) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityEmail();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityBankCard) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityBankCard();
                                } else if (old_entity instanceof TLRPC.TL_messageEntityPhone) {
                                    found = true;
                                    entity = new TLRPC.TL_messageEntityPhone();
                                }
                                if(found){
                                    entity.offset = old_entity.offset;
                                    entity.length = old_entity.length;
                                    copyEntities.remove(i);
                                    break;
                                }
                            }
                            break;
                    }
                    if(entity != null){
                        entity.offset = offset;
                        entity.length = length;
                        returnEntities.add(entity);
                    }
                }
            }
        }
        return new Object[]{
            new_text,
            returnEntities,
        };
    }

    public static void showTranslationProviderSelector(Context context, View view, MessagesStorage.BooleanCallback callback, Theme.ResourcesProvider resourcesProvider) {
        Pair<ArrayList<String>, ArrayList<Integer>> providers = getProviders();
        ArrayList<String> names = providers.first;
        ArrayList<Integer> types = providers.second;
        if (names == null || types == null) {
            return;
        }
        PopupHelper.show(names, LocaleController.getString("OwlgramTranslationProvider", R.string.OwlgramTranslationProvider), types.indexOf(OwlConfig.translationProvider), context, view, i -> {
            BaseTranslator translator = getTranslator(types.get(i));
            String targetLanguage = translator.getTargetLanguage(OwlConfig.translationTarget);

            if (translator.supportLanguage(targetLanguage)) {
                OwlConfig.setTranslationProvider(types.get(i));
                if (callback != null) callback.run(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider)
                        .setMessage(LocaleController.getString("OwlgramTranslateApiUnsupported", R.string.OwlgramTranslateApiUnsupported));
                if ("app".equals(OwlConfig.translationTarget)) {
                    builder.setPositiveButton(LocaleController.getString("OwlgramUseGoogleTranslate", R.string.OwlgramUseGoogleTranslate), (dialog, which) -> {
                        OwlConfig.setTranslationProvider(Translator.PROVIDER_GOOGLE);
                        if (callback != null) callback.run(false);
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                } else if (translator.supportLanguage(translator.getCurrentAppLanguage())) {
                    builder.setPositiveButton(LocaleController.getString("OwlgramResetLanguage", R.string.OwlgramResetLanguage), (dialog, which) -> {
                        OwlConfig.setTranslationProvider(types.get(i));
                        OwlConfig.setTranslationTarget("app");
                        if (callback != null) callback.run(false);
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                } else {
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                }
                builder.show();
            }
        }, resourcesProvider);
    }

    public static BaseTranslator getCurrentTranslator() {
        return getTranslator(OwlConfig.translationProvider);
    }

    public static BaseTranslator getTranslator(int type) {
        switch (type) {
            case PROVIDER_YANDEX:
                return YandexTranslator.getInstance();
            case PROVIDER_DEEPL:
                return DeepLTranslator.getInstance();
            case PROVIDER_NIU:
                return NiuTranslator.getInstance();
            case PROVIDER_GOOGLE:
            default:
                return GoogleAppTranslator.getInstance();
        }
    }

    public static void translate(Object query, TranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();

        String language = translator.getCurrentTargetLanguage();

        if (!translator.supportLanguage(language)) {
            translateCallBack.onError(new UnsupportedTargetLanguageException());
        } else {
            translator.startTask(query, language, translateCallBack);
        }
    }

    public interface TranslateCallBack {
        void onSuccess(Object translation);

        void onError(Exception e);
    }

    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }
}

