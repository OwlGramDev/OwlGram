package it.owlgram.android.settings;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.DcStyleSelector;
import it.owlgram.android.helpers.PopupHelper;
import it.owlgram.android.translator.BaseTranslator;
import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;
import it.owlgram.android.translator.TranslatorHelper;

public class OwlgramGeneralSettings extends BaseSettingsActivity {
    private final boolean supportLanguageDetector;

    private int divisorPrivacyRow;
    private int privacyHeaderRow;
    private int phoneNumberSwitchRow;
    private int phoneContactsSwitchRow;
    private int translationHeaderRow;
    private int showTranslateButtonRow;
    private int translationStyle;
    private int translationProviderSelectRow;
    private int destinationLanguageSelectRow;
    private int doNotTranslateSelectRow;
    private int autoTranslateRow;
    private int keepMarkdownRow;
    private int divisorTranslationRow;
    private int hintTranslation1;
    private int hintTranslation2;
    private int dcIdSettingsHeaderRow;
    private int dcStyleSelectorRow;
    private int dcIdRow;
    private int idTypeRow;
    private int divisorDCIdRow;
    private int hintIdRow;
    private int notificationHeaderRow;
    private int notificationAccentRow;
    private int dividerNotificationRow;
    private int callHeaderRow;
    private int confirmCallSwitchRow;
    private int deepLFormalityRow;

    // TYPES
    private static final int TYPE_DC_STYLE_SELECTOR = 200;

    public OwlgramGeneralSettings() {
        supportLanguageDetector = LanguageDetector.hasSupport();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("General", R.string.General);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == phoneNumberSwitchRow) {
            OwlConfig.toggleHidePhone();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.hidePhoneNumber);
            }
            parentLayout.rebuildAllFragmentViews(false, false);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (position == phoneContactsSwitchRow) {
            OwlConfig.toggleHideContactNumber();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.hideContactNumber);
            }
        } else if (position == dcIdRow) {
            OwlConfig.toggleShowIDAndDC();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.showIDAndDC);
            }
            parentLayout.rebuildAllFragmentViews(false, false);
        } else if (position == translationStyle) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("TranslatorTypeOwl", R.string.TranslatorTypeOwl));
            types.add(BaseTranslator.INLINE_STYLE);
            arrayList.add(LocaleController.getString("TranslatorTypeTG", R.string.TranslatorTypeTG));
            types.add(BaseTranslator.DIALOG_STYLE);
            PopupHelper.show(arrayList, LocaleController.getString("TranslatorType", R.string.TranslatorType), types.indexOf(OwlConfig.translatorStyle), context, i -> {
                OwlConfig.setTranslatorStyle(types.get(i));
                listAdapter.notifyItemChanged(translationStyle, PARTIAL);
            });
        } else if (position == translationProviderSelectRow) {
            final int oldProvider = OwlConfig.translationProvider;
            Translator.showTranslationProviderSelector(context, param -> {
                if (param) {
                    listAdapter.notifyItemChanged(translationProviderSelectRow, PARTIAL);
                } else {
                    listAdapter.notifyItemRangeChanged(translationProviderSelectRow, 2, PARTIAL);
                }
                listAdapter.notifyItemChanged(hintTranslation2);
                if (oldProvider != OwlConfig.translationProvider) {
                    boolean oldProviderSupport = TranslatorHelper.isSupportHTMLMode(oldProvider);
                    boolean newProviderSupport = TranslatorHelper.isSupportHTMLMode();
                    if (oldProviderSupport != newProviderSupport) {
                        if (newProviderSupport) {
                            listAdapter.notifyItemInserted(autoTranslateRow + 1);
                        } else {
                            listAdapter.notifyItemRemoved(autoTranslateRow + 1);
                        }
                        listAdapter.notifyItemChanged(autoTranslateRow);
                    }
                    if (oldProvider == Translator.PROVIDER_DEEPL) {
                        listAdapter.notifyItemChanged(destinationLanguageSelectRow, PARTIAL);
                        listAdapter.notifyItemRemoved(deepLFormalityRow);
                        updateRowsId();
                    } else if (OwlConfig.translationProvider == Translator.PROVIDER_DEEPL) {
                        updateRowsId();
                        listAdapter.notifyItemChanged(destinationLanguageSelectRow, PARTIAL);
                        listAdapter.notifyItemInserted(deepLFormalityRow);
                    } else if (oldProviderSupport != newProviderSupport) {
                        updateRowsId();
                    }
                    listAdapter.notifyItemChanged(doNotTranslateSelectRow, PARTIAL);
                }
            });
        } else if (position == destinationLanguageSelectRow) {
            presentFragment(new SelectLanguageSettings());
        } else if (position == doNotTranslateSelectRow) {
            if (!supportLanguageDetector) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            presentFragment(new DoNotTranslateSettings());
        } else if (position == deepLFormalityRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault));
            types.add(DeepLTranslator.FORMALITY_DEFAULT);
            arrayList.add(LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore));
            types.add(DeepLTranslator.FORMALITY_MORE);
            arrayList.add(LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess));
            types.add(DeepLTranslator.FORMALITY_LESS);
            PopupHelper.show(arrayList, LocaleController.getString("DeepLFormality", R.string.DeepLFormality), types.indexOf(OwlConfig.deepLFormality), context, i -> {
                OwlConfig.setDeepLFormality(types.get(i));
                listAdapter.notifyItemChanged(deepLFormalityRow, PARTIAL);
            });
        } else if (position == confirmCallSwitchRow) {
            OwlConfig.toggleConfirmCall();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.confirmCall);
            }
        } else if (position == notificationAccentRow) {
            OwlConfig.toggleAccentColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.accentAsNotificationColor);
            }
        } else if (position == idTypeRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add("Bot API");
            types.add(0);
            arrayList.add("Telegram API");
            types.add(1);
            PopupHelper.show(arrayList, LocaleController.getString("IDType", R.string.IDType), types.indexOf(OwlConfig.idType), context, i -> {
                OwlConfig.setIdType(types.get(i));
                listAdapter.notifyItemChanged(idTypeRow, PARTIAL);
                parentLayout.rebuildAllFragmentViews(false, false);
            });
        } else if (position == autoTranslateRow) {
            if (!supportLanguageDetector) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            OwlConfig.toggleAutoTranslate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.autoTranslate);
            }
        } else if (position == keepMarkdownRow) {
            OwlConfig.toggleKeepTranslationMarkdown();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.keepTranslationMarkdown);
            }
        } else if (position == showTranslateButtonRow) {
            OwlConfig.toggleShowTranslate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.showTranslate);
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        privacyHeaderRow = rowCount++;
        phoneNumberSwitchRow = rowCount++;
        phoneContactsSwitchRow = rowCount++;
        divisorPrivacyRow = rowCount++;
        translationHeaderRow = rowCount++;
        showTranslateButtonRow = rowCount++;
        translationStyle = rowCount++;
        translationProviderSelectRow = rowCount++;
        destinationLanguageSelectRow = rowCount++;
        doNotTranslateSelectRow = rowCount++;
        deepLFormalityRow = OwlConfig.translationProvider == Translator.PROVIDER_DEEPL ? rowCount++ : -1;
        autoTranslateRow = rowCount++;
        keepMarkdownRow = TranslatorHelper.isSupportHTMLMode() ? rowCount++ : -1;
        divisorTranslationRow = rowCount++;
        hintTranslation1 = rowCount++;
        hintTranslation2 = rowCount++;
        dcIdSettingsHeaderRow = rowCount++;
        dcStyleSelectorRow = rowCount++;
        dcIdRow = rowCount++;
        idTypeRow = rowCount++;
        divisorDCIdRow = rowCount++;
        hintIdRow = rowCount++;
        notificationHeaderRow = rowCount++;
        notificationAccentRow = rowCount++;
        dividerNotificationRow = rowCount++;
        callHeaderRow = rowCount++;
        confirmCallSwitchRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (holder.getItemViewType()) {
                case TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == privacyHeaderRow) {
                        headerCell.setText(LocaleController.getString("PrivacyTitle", R.string.PrivacyTitle));
                    } else if (position == translationHeaderRow) {
                        headerCell.setText(LocaleController.getString("TranslateMessages", R.string.TranslateMessages));
                    } else if (position == callHeaderRow) {
                        headerCell.setText(LocaleController.getString("Calls", R.string.Calls));
                    } else if (position == dcIdSettingsHeaderRow) {
                        headerCell.setText(LocaleController.getString("DC_IDSettings", R.string.DC_IDSettings));
                    } else if (position == notificationHeaderRow) {
                        headerCell.setText(LocaleController.getString("Notifications", R.string.Notifications));
                    }
                    break;
                case TYPE_SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == phoneNumberSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("HidePhone", R.string.HidePhone), LocaleController.getString("HidePhoneDesc", R.string.HidePhoneDesc), OwlConfig.hidePhoneNumber, true, true);
                    } else if (position == phoneContactsSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("HidePhoneOthers", R.string.HidePhoneOthers), LocaleController.getString("HidePhoneOthersDesc", R.string.HidePhoneOthersDesc), OwlConfig.hideContactNumber, true, true);
                    } else if (position == dcIdRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowID_DC", R.string.ShowID_DC), OwlConfig.showIDAndDC, true);
                    } else if (position == confirmCallSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("ConfirmCalls", R.string.ConfirmCalls), LocaleController.getString("ConfirmCallsDesc", R.string.ConfirmCallsDesc), OwlConfig.confirmCall, true, true);
                    } else if (position == notificationAccentRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AccentAsNotificationColor", R.string.AccentAsNotificationColor), OwlConfig.accentAsNotificationColor, true);
                    } else if (position == autoTranslateRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AutoTranslate", R.string.AutoTranslate), LocaleController.getString("AutoTranslateDesc", R.string.AutoTranslateDesc), OwlConfig.autoTranslate && supportLanguageDetector, true, keepMarkdownRow != -1);
                        if (!supportLanguageDetector) textCheckCell.setAlpha(0.5f);
                    } else if (position == keepMarkdownRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("KeepMarkdown", R.string.KeepMarkdown), LocaleController.getString("KeepMarkdownDesc", R.string.KeepMarkdownDesc), OwlConfig.keepTranslationMarkdown, true, false);
                    } else if (position == showTranslateButtonRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowTranslateButton", R.string.ShowTranslateButton), OwlConfig.showTranslate, true);
                    }
                    break;
                case TYPE_SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == translationProviderSelectRow) {
                        Pair<ArrayList<String>, ArrayList<Integer>> providers = Translator.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<Integer> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(OwlConfig.translationProvider);
                        if (index < 0) {
                            textSettingsCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), names.get(Translator.PROVIDER_GOOGLE), partial,true);
                        } else {
                            textSettingsCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), names.get(index), partial,true);
                        }
                    } else if (position == destinationLanguageSelectRow) {
                        String language = OwlConfig.translationTarget;
                        CharSequence value;
                        if (language.equals("app")) {
                            value = LocaleController.getString("Default", R.string.Default);
                        } else {
                            Locale locale = Locale.forLanguageTag(language);
                            if (!TextUtils.isEmpty(locale.getScript())) {
                                value = HtmlCompat.fromHtml(AndroidUtilities.capitalize(locale.getDisplayScript()), HtmlCompat.FROM_HTML_MODE_LEGACY);
                            } else {
                                value = AndroidUtilities.capitalize(locale.getDisplayName());
                            }
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage), value, partial,true);
                    } else if (position == doNotTranslateSelectRow) {
                        String doNotTranslateCellValue = null;
                        HashSet<String> langCodes = DoNotTranslateSettings.getRestrictedLanguages(false);
                        if (langCodes.size() == 1) {
                            try {
                                String language = langCodes.iterator().next();
                                if (language.equals("app")) {
                                    doNotTranslateCellValue = LocaleController.getString("Default", R.string.Default);
                                } else {
                                    Locale locale = Locale.forLanguageTag(language);
                                    if (!TextUtils.isEmpty(locale.getScript())) {
                                        doNotTranslateCellValue = HtmlCompat.fromHtml(AndroidUtilities.capitalize(locale.getDisplayScript()), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                    } else {
                                        doNotTranslateCellValue = AndroidUtilities.capitalize(locale.getDisplayName());
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        } else if (langCodes.size() == 0) {
                            doNotTranslateCellValue = LocaleController.getString("EmptyExceptions", R.string.EmptyExceptions);
                        }
                        if (doNotTranslateCellValue == null)
                            doNotTranslateCellValue = String.format(LocaleController.getPluralString("Languages", langCodes.size()), langCodes.size());
                        if (!supportLanguageDetector) {
                            doNotTranslateCellValue = LocaleController.getString("EmptyExceptions", R.string.EmptyExceptions);
                            textSettingsCell.setAlpha(0.5f);
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("DoNotTranslate", R.string.DoNotTranslate), doNotTranslateCellValue, partial,true);
                    } else if (position == deepLFormalityRow) {
                        String value;
                        switch (OwlConfig.deepLFormality) {
                            case DeepLTranslator.FORMALITY_DEFAULT:
                                value = LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault);
                                break;
                            case DeepLTranslator.FORMALITY_MORE:
                                value = LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore);
                                break;
                            case DeepLTranslator.FORMALITY_LESS:
                            default:
                                value = LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("DeepLFormality", R.string.DeepLFormality), value, partial,true);
                    } else if (position == translationStyle) {
                        String value;
                        switch (OwlConfig.translatorStyle) {
                            case BaseTranslator.INLINE_STYLE:
                                value = LocaleController.getString("TranslatorTypeOwl", R.string.TranslatorTypeOwl);
                                break;
                            case BaseTranslator.DIALOG_STYLE:
                            default:
                                value = LocaleController.getString("TranslatorTypeTG", R.string.TranslatorTypeTG);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("TranslatorType", R.string.TranslatorType), value, partial,true);
                    } else if (position == idTypeRow) {
                        String value;
                        switch (OwlConfig.idType) {
                            case 0:
                                value = "Bot API";
                                break;
                            default:
                            case 1:
                                value = "Telegram API";
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("IDType", R.string.IDType), value, partial,false);
                    }
                    break;
                case TYPE_TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == hintTranslation1) {
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.getString("TranslateMessagesInfo1", R.string.TranslateMessagesInfo1));
                    } else if (position == hintTranslation2) {
                        Pair<ArrayList<String>, ArrayList<Integer>> providers = Translator.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<Integer> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(OwlConfig.translationProvider);
                        if (index < 0) {
                            index = types.indexOf(Translator.PROVIDER_GOOGLE);
                        }
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.formatString("TranslationProviderInfo", R.string.TranslationProviderInfo, names.get(index)));
                    } else if (position == hintIdRow) {
                        textInfoPrivacyCell.setTopPadding(0);
                        textInfoPrivacyCell.setText(LocaleController.getString("IDTypeAbout", R.string.IDTypeAbout));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            int position = holder.getAdapterPosition();
            if (position == autoTranslateRow || position == doNotTranslateSelectRow) {
                return supportLanguageDetector;
            }
            return type == TYPE_SWITCH || type == TYPE_SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(int viewType) {
            View view = null;
            if (viewType == TYPE_DC_STYLE_SELECTOR) {
                view = new DcStyleSelector(context) {
                    @Override
                    protected void onSelectedStyle() {
                        super.onSelectedStyle();
                        parentLayout.rebuildAllFragmentViews(false, false);
                        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    }
                };
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == divisorPrivacyRow || position == divisorTranslationRow || position == divisorDCIdRow ||
                    position == dividerNotificationRow) {
                return TYPE_SHADOW;
            } else if (position == privacyHeaderRow || position == translationHeaderRow || position == callHeaderRow ||
                    position == dcIdSettingsHeaderRow || position == notificationHeaderRow) {
                return TYPE_HEADER;
            } else if (position == phoneNumberSwitchRow || position == phoneContactsSwitchRow || position == dcIdRow ||
                    position == confirmCallSwitchRow || position == notificationAccentRow || position == autoTranslateRow ||
                    position == keepMarkdownRow || position == showTranslateButtonRow) {
                return TYPE_SWITCH;
            } else if (position == translationProviderSelectRow || position == destinationLanguageSelectRow || position == deepLFormalityRow ||
                    position == translationStyle || position == doNotTranslateSelectRow || position == idTypeRow) {
                return TYPE_SETTINGS;
            } else if (position == hintTranslation1 || position == hintTranslation2 || position == hintIdRow) {
                return TYPE_TEXT_HINT_WITH_PADDING;
            } else if (position == dcStyleSelectorRow) {
                return TYPE_DC_STYLE_SELECTOR;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
