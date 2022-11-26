package it.owlgram.android.translator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.helpers.PopupHelper;

public class Translator {

    public static final int PROVIDER_GOOGLE = 1;
    public static final int PROVIDER_YANDEX = 4;
    public static final int PROVIDER_DEEPL = 5;
    public static final int PROVIDER_NIU = 9;
    public static final int PROVIDER_DUCKDUCKGO = 13;
    public static final int PROVIDER_TELEGRAM = 14;

    @SuppressLint("StaticFieldLeak")
    private static AlertDialog progressDialog;

    public static void showTranslateDialog(Context context, String query, Runnable callback) {
        showTranslateDialog(context, query, callback, null);
    }

    public static boolean isSupportedOutputLang(int provider) {
        return provider == PROVIDER_GOOGLE ||
                provider == PROVIDER_YANDEX ||
                provider == PROVIDER_DEEPL ||
                provider == PROVIDER_DUCKDUCKGO;
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
            public void onSuccess(BaseTranslator.Result result) {
                try {
                    progressDialog.dismiss();
                } catch (Exception ignore) {
                }
                TextView messageTextView = new TextView(context);
                messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
                messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                messageTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
                messageTextView.setTextIsSelectable(true);
                messageTextView.setText((String) result.translation);
                messageTextView.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(4), AndroidUtilities.dp(24), AndroidUtilities.dp(4));

                AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
                builder.setView(messageTextView);
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                builder.setNeutralButton(LocaleController.getString("Copy", R.string.Copy), (dialog, which) -> {
                    AndroidUtilities.addToClipboard((String) result.translation);
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
            builder.setMessage(LocaleController.getString("TranslateApiUnsupported", R.string.TranslateApiUnsupported));
            builder.setPositiveButton(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, resourcesProvider));
        } else {
            if (e instanceof BaseTranslator.Http429Exception) {
                builder.setTitle(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
                builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
            } else if (e != null && e.getLocalizedMessage() != null) {
                builder.setTitle(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
                builder.setMessage(e.getLocalizedMessage());
            } else {
                builder.setMessage(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
            }
            if (onRetry != null) {
                builder.setPositiveButton(LocaleController.getString("Retry", R.string.Retry), (dialog, which) -> onRetry.run());
            }
            builder.setNeutralButton(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, resourcesProvider));
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show();
    }

    public static Pair<ArrayList<String>, ArrayList<Integer>> getProviders() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        names.add(LocaleController.getString("ProviderDeepLTranslate", R.string.ProviderDeepLTranslate));
        types.add(Translator.PROVIDER_DEEPL);
        names.add(LocaleController.getString("ProviderGoogleTranslate", R.string.ProviderGoogleTranslate));
        types.add(Translator.PROVIDER_GOOGLE);
        names.add(LocaleController.getString("ProviderDuckDuckGo", R.string.ProviderDuckDuckGo));
        types.add(Translator.PROVIDER_DUCKDUCKGO);
        names.add(LocaleController.getString("ProviderYandex", R.string.ProviderYandex));
        types.add(Translator.PROVIDER_YANDEX);
        names.add(LocaleController.getString("ProviderTelegram", R.string.ProviderTelegram));
        types.add(Translator.PROVIDER_TELEGRAM);
        return new Pair<>(names, types);
    }

    public static void showTranslationTargetSelector(Context context, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        showTranslationTargetSelector(context, false, callback, resourcesProvider);
    }

    public static void showTranslationTargetSelector(Context context, boolean isKeyboard, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        BaseTranslator translator = Translator.getCurrentTranslator();
        ArrayList<String> targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        ArrayList<CharSequence> names = new ArrayList<>();
        for (String language : targetLanguages) {
            Locale locale = Locale.forLanguageTag(language);
            if (!TextUtils.isEmpty(locale.getScript())) {
                names.add(HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
            } else {
                names.add(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale))));
            }
        }
        AndroidUtilities.selectionSort(names, targetLanguages);

        targetLanguages.add(0, "app");
        names.add(0, LocaleController.getString("Default", R.string.Default));

        PopupHelper.show(names, LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage), targetLanguages.indexOf(isKeyboard ? OwlConfig.translationKeyboardTarget : OwlConfig.translationTarget), context, i -> {
            if (isKeyboard) {
                OwlConfig.setTranslationKeyboardTarget(targetLanguages.get(i));
            } else {
                OwlConfig.setTranslationTarget(targetLanguages.get(i));
            }
            callback.run();
        }, resourcesProvider);
    }

    public static void showTranslationProviderSelector(Context context, MessagesStorage.BooleanCallback callback) {
        showTranslationProviderSelector(context, callback, null);
    }

    public static void showTranslationProviderSelector(Context context, MessagesStorage.BooleanCallback callback, Theme.ResourcesProvider resourcesProvider) {
        Pair<ArrayList<String>, ArrayList<Integer>> providers = getProviders();
        ArrayList<String> names = providers.first;
        ArrayList<Integer> types = providers.second;
        if (names == null || types == null) {
            return;
        }
        int index = types.indexOf(OwlConfig.translationProvider);
        if (index == -1) {
            index = types.indexOf(Translator.PROVIDER_GOOGLE);
        }
        PopupHelper.show(names, LocaleController.getString("TranslationProvider", R.string.TranslationProvider), index, context, i -> {
            BaseTranslator translator = getTranslator(types.get(i));
            String targetLanguage = translator.getTargetLanguage(OwlConfig.translationTarget);

            if (translator.supportLanguage(targetLanguage)) {
                OwlConfig.setTranslationProvider(types.get(i));
                if (callback != null) callback.run(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider)
                        .setMessage(LocaleController.getString("TranslateApiUnsupported", R.string.TranslateApiUnsupported));
                if ("app".equals(OwlConfig.translationTarget)) {
                    builder.setPositiveButton(LocaleController.getString("UseGoogleTranslate", R.string.UseGoogleTranslate), (dialog, which) -> {
                        OwlConfig.setTranslationProvider(Translator.PROVIDER_GOOGLE);
                        if (callback != null) callback.run(false);
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                } else if (translator.supportLanguage(translator.getCurrentAppLanguage())) {
                    builder.setPositiveButton(LocaleController.getString("ResetLanguage", R.string.ResetLanguage), (dialog, which) -> {
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
            case PROVIDER_DUCKDUCKGO:
                return DuckDuckGoTranslator.getInstance();
            case PROVIDER_TELEGRAM:
                return TelegramTranslator.getInstance();
            case PROVIDER_GOOGLE:
            default:
                return GoogleAppTranslator.getInstance();
        }
    }

    public static void translate(Object query, TranslateCallBack translateCallBack) {
        translate(query, false, translateCallBack);
    }

    public static void translate(Object query, boolean isKeyboard, TranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();
        String language = isKeyboard ? translator.getCurrentTargetKeyboardLanguage() : translator.getCurrentTargetLanguage();
        if (!translator.supportLanguage(language)) {
            translateCallBack.onError(new UnsupportedTargetLanguageException());
        } else {
            translator.startTask(query, language, translateCallBack);
        }
    }

    public interface TranslateCallBack {
        void onSuccess(BaseTranslator.Result result);

        void onError(Exception e);
    }


    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }
}

