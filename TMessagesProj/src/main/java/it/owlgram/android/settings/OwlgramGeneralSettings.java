package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Locale;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.DrawerProfilePreviewCell;
import it.owlgram.android.helpers.PopupHelper;
import it.owlgram.android.translator.DeepLTranslator;
import it.owlgram.android.translator.Translator;

public class OwlgramGeneralSettings extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;

    private int divisorPrivacyRow;
    private int privacyHeaderRow;
    private int phoneNumberSwitchRow;
    private int phoneContactsSwitchRow;
    private int appearanceHeaderRow;
    private int useSystemFontRow;
    private int useSystemEmojiRow;
    private int statusBarSwitchRow;
    private int messageTimeSwitchRow;
    private int roundedNumberSwitchRow;
    private int divisorAppearanceRow;
    private int translationHeaderRow;
    private int translationProviderSelectRow;
    private int destinationLanguageSelectRow;
    private int divisorTranslationRow;
    private int callHeaderRow;
    private int confirmCallSwitchRow;
    private int deepLFormalityRow;
    private int drawerRow;
    private int drawerAvatarAsBackgroundRow;
    private int drawerBlurBackgroundRow;
    private int drawerDarkenBackgroundRow;
    private int drawerDividerRow;

    private DrawerProfilePreviewCell profilePreviewCell;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("General", R.string.General));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == phoneNumberSwitchRow) {
                OwlConfig.toggleHidePhone();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.hidePhoneNumber);
                }
                parentLayout.rebuildAllFragmentViews(false, false);
            }else if (position == phoneContactsSwitchRow) {
                OwlConfig.toggleHideContactNumber();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.hideContactNumber);
                }
            }else if (position == statusBarSwitchRow) {
                SharedConfig.toggleNoStatusBar();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.noStatusBar);
                }
                if (getParentActivity() != null && Build.VERSION.SDK_INT >= 21) {
                    if (SharedConfig.noStatusBar) {
                        getParentActivity().getWindow().setStatusBarColor(0);
                    } else {
                        getParentActivity().getWindow().setStatusBarColor(0x33000000);
                    }
                }
            }else if (position == messageTimeSwitchRow) {
                OwlConfig.toggleFullTime();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.fullTime);
                }
                LocaleController.getInstance().recreateFormatters();
            }else if (position == roundedNumberSwitchRow) {
                OwlConfig.toggleRoundedNumbers();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.roundedNumbers);
                }
            } else if (position == translationProviderSelectRow) {
                final int oldProvider = OwlConfig.translationProvider;
                Translator.showTranslationProviderSelector(context, null, param -> {
                    if (param) {
                        listAdapter.notifyItemChanged(translationProviderSelectRow);
                    } else {
                        listAdapter.notifyItemRangeChanged(translationProviderSelectRow, 2);
                    }
                    if (oldProvider != OwlConfig.translationProvider) {
                        if (oldProvider == Translator.PROVIDER_DEEPL) {
                            listAdapter.notifyItemChanged(destinationLanguageSelectRow);
                            listAdapter.notifyItemRemoved(deepLFormalityRow);
                            updateRowsId(false);
                        } else if (OwlConfig.translationProvider == Translator.PROVIDER_DEEPL) {
                            updateRowsId(false);
                            listAdapter.notifyItemChanged(destinationLanguageSelectRow);
                            listAdapter.notifyItemInserted(deepLFormalityRow);
                        }
                    }
                });
            } else if (position == destinationLanguageSelectRow) {
                Translator.showTranslationTargetSelector(context, null, () -> listAdapter.notifyItemChanged(destinationLanguageSelectRow));
            } else if (position == deepLFormalityRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add(LocaleController.getString("OwlgramDeepLFormalityDefault", R.string.OwlgramDeepLFormalityDefault));
                types.add(DeepLTranslator.FORMALITY_DEFAULT);
                arrayList.add(LocaleController.getString("OwlgramDeepLFormalityMore", R.string.OwlgramDeepLFormalityMore));
                types.add(DeepLTranslator.FORMALITY_MORE);
                arrayList.add(LocaleController.getString("OwlgramDeepLFormalityLess", R.string.OwlgramDeepLFormalityLess));
                types.add(DeepLTranslator.FORMALITY_LESS);
                PopupHelper.show(arrayList, LocaleController.getString("OwlgramDeepLFormality", R.string.OwlgramDeepLFormality), types.indexOf(OwlConfig.deepLFormality), context, null, i -> {
                    OwlConfig.setDeepLFormality(types.get(i));
                    listAdapter.notifyItemChanged(deepLFormalityRow);
                });
            } else if (position == confirmCallSwitchRow) {
                OwlConfig.toggleConfirmCall();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.confirmCall);
                }
            } else if (position == useSystemFontRow) {
                OwlConfig.toggleUseSystemFont();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.useSystemFont);
                }
                parentLayout.rebuildAllFragmentViews(true, true);
            } else if (position == drawerAvatarAsBackgroundRow) {
                OwlConfig.toggleAvatarAsDrawerBackground();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.avatarAsDrawerBackground);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(profilePreviewCell);
                }
                listAdapter.notifyItemChanged(drawerRow, new Object());
                if (OwlConfig.avatarAsDrawerBackground) {
                    updateRowsId(false);
                    listAdapter.notifyItemRangeInserted(drawerBlurBackgroundRow, 2);
                } else {
                    listAdapter.notifyItemRangeRemoved(drawerBlurBackgroundRow, 2);
                    updateRowsId(false);
                }
            } else if (position == drawerBlurBackgroundRow) {
                OwlConfig.toggleAvatarBackgroundBlur();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.avatarBackgroundBlur);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == drawerDarkenBackgroundRow) {
                OwlConfig.toggleAvatarBackgroundDarken();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.avatarBackgroundDarken);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            }
        });
        return fragmentView;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        drawerBlurBackgroundRow = -1;
        drawerDarkenBackgroundRow = -1;

        drawerRow = rowCount++;
        drawerAvatarAsBackgroundRow = rowCount++;
        if(OwlConfig.avatarAsDrawerBackground) {
            drawerBlurBackgroundRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        privacyHeaderRow = rowCount++;
        phoneNumberSwitchRow = rowCount++;
        phoneContactsSwitchRow = rowCount++;
        divisorPrivacyRow = rowCount++;
        appearanceHeaderRow = rowCount++;
        useSystemFontRow = rowCount++;
        useSystemEmojiRow = rowCount++;
        statusBarSwitchRow = rowCount++;
        messageTimeSwitchRow = rowCount++;
        roundedNumberSwitchRow = rowCount++;
        divisorAppearanceRow = rowCount++;
        translationHeaderRow = rowCount++;
        translationProviderSelectRow = rowCount++;
        destinationLanguageSelectRow = rowCount++;
        deepLFormalityRow = OwlConfig.translationProvider == Translator.PROVIDER_DEEPL ? rowCount++:-1;
        divisorTranslationRow = rowCount++;
        callHeaderRow = rowCount++;
        confirmCallSwitchRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }
    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == privacyHeaderRow) {
                        headerCell.setText(LocaleController.getString("PrivacyTitle", R.string.PrivacyTitle));
                    }else if (position == appearanceHeaderRow) {
                        headerCell.setText(LocaleController.getString("Appearance", R.string.Appearance));
                    }else if (position == translationHeaderRow) {
                        headerCell.setText(LocaleController.getString("PassportTranslation", R.string.PassportTranslation));
                    }else if (position == callHeaderRow) {
                        headerCell.setText(LocaleController.getString("Calls", R.string.Calls));
                    }
                    break;
                case 3:
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(true, null);
                    if(position == phoneNumberSwitchRow){
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramHidePhone", R.string.OwlgramHidePhone), OwlConfig.hidePhoneNumber, true);
                    } else if (position == phoneContactsSwitchRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramHidePhoneContacts", R.string.OwlgramHidePhoneContacts), OwlConfig.hideContactNumber, true);
                    } else if (position == statusBarSwitchRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramHideStatusBar", R.string.OwlgramHideStatusBar), SharedConfig.noStatusBar, true);
                    }else if (position == messageTimeSwitchRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramMessageTime", R.string.OwlgramMessageTime), OwlConfig.fullTime, true);
                    }else if (position == roundedNumberSwitchRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramRoundedNumbers", R.string.OwlgramRoundedNumbers), OwlConfig.roundedNumbers, true);
                    }else if (position == confirmCallSwitchRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramConfirmCall", R.string.OwlgramConfirmCall), OwlConfig.confirmCall, true);
                    }else if (position == useSystemFontRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramUseSystemFont", R.string.OwlgramUseSystemFont), OwlConfig.useSystemFont, true);
                    }else if (position == useSystemEmojiRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramUseSystemEmoji", R.string.OwlgramUseSystemEmoji), OwlConfig.useSystemEmoji, true);
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramAvatarAsBackground", R.string.OwlgramAvatarAsBackground), OwlConfig.avatarAsDrawerBackground, drawerBlurBackgroundRow != -1);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramAvatarBlur", R.string.OwlgramAvatarBlur), OwlConfig.avatarBackgroundBlur, true);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("OwlgramAvatarDarken", R.string.OwlgramAvatarDarken), OwlConfig.avatarBackgroundDarken, false);
                    }
                    break;
                case 4:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if(position == translationProviderSelectRow){
                        Pair<ArrayList<String>, ArrayList<Integer>> providers = Translator.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<Integer> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(OwlConfig.translationProvider);
                        if (index < 0) {
                            textSettingsCell.setTextAndValue(LocaleController.getString("OwlgramTranslationProviderShort", R.string.OwlgramTranslationProviderShort), "", true);
                        } else {
                            String value = names.get(index);
                            textSettingsCell.setTextAndValue(LocaleController.getString("OwlgramTranslationProviderShort", R.string.OwlgramTranslationProviderShort), value, true);
                        }
                    } else if (position == destinationLanguageSelectRow) {
                        String language = OwlConfig.translationTarget;
                        CharSequence value;
                        if (language.equals("app")) {
                            value = LocaleController.getString("Default", R.string.Default);
                        } else {
                            Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Locale.forLanguageTag(language) : new Locale(language);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(locale.getScript())) {
                                value = HtmlCompat.fromHtml(AndroidUtilities.capitalize(locale.getDisplayScript()), HtmlCompat.FROM_HTML_MODE_LEGACY);
                            } else {
                                value = AndroidUtilities.capitalize(locale.getDisplayName());
                            }
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("OwlgramTranslationTarget", R.string.OwlgramTranslationTarget), value, true);
                    } else if (position == deepLFormalityRow) {
                        String value;
                        switch (OwlConfig.deepLFormality) {
                            case DeepLTranslator.FORMALITY_DEFAULT:
                                value = LocaleController.getString("OwlgramDeepLFormalityDefault", R.string.OwlgramDeepLFormalityDefault);
                                break;
                            case DeepLTranslator.FORMALITY_MORE:
                                value = LocaleController.getString("OwlgramDeepLFormalityMore", R.string.OwlgramDeepLFormalityMore);
                                break;
                            case DeepLTranslator.FORMALITY_LESS:
                            default:
                                value = LocaleController.getString("OwlgramDeepLFormalityLess", R.string.OwlgramDeepLFormalityLess);
                                break;
                        }
                        textSettingsCell.setTextAndValue(LocaleController.getString("OwlgramDeepLFormality", R.string.OwlgramDeepLFormality), value, false);
                    }
                    break;
                case 5:
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
            }
        }
        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 4;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = profilePreviewCell = new DrawerProfilePreviewCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }
        @Override
        public int getItemViewType(int position) {
            if(position == divisorPrivacyRow || position == divisorAppearanceRow || position == divisorTranslationRow || position == drawerDividerRow){
                return 1;
            }else if(position == privacyHeaderRow || position == appearanceHeaderRow || position == translationHeaderRow || position == callHeaderRow){
                return 2;
            }else if(position == phoneNumberSwitchRow || position == phoneContactsSwitchRow || position == statusBarSwitchRow ||
                    position == roundedNumberSwitchRow || position == messageTimeSwitchRow || position == confirmCallSwitchRow ||
                    position == useSystemFontRow || position == useSystemEmojiRow || position == drawerAvatarAsBackgroundRow ||
                    position == drawerDarkenBackgroundRow || position == drawerBlurBackgroundRow
            ){
                return 3;
            }else if(position == translationProviderSelectRow || position == destinationLanguageSelectRow || position == deepLFormalityRow){
                return 4;
            } else if (position == drawerRow) {
                return 5;
            }
            return 1;
        }
    }
}
