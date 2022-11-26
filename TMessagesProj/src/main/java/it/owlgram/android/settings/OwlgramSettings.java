package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

import it.owlgram.android.Crashlytics;
import it.owlgram.android.OwlConfig;
import it.owlgram.android.StoreUtils;

public class OwlgramSettings extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;

    private int divisorInfoRow;
    private int categoryHeaderRow;
    private int generalSettingsRow;
    private int appearanceSettingsRow;
    private int chatSettingsRow;
    private int updateSettingsRow;
    private int experimentalSettingsRow;
    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int groupUpdatesRow;
    private int sourceCodeRow;
    private int supportTranslationRow;
    private int supportDonationRow;
    private int bugReportRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlSetting", R.string.OwlSetting));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    OwlConfig.shareSettings(getParentActivity());
                } else if (id == 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
                    builder.setMessage(LocaleController.getString("ResetSettingsAlert", R.string.ResetSettingsAlert));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString("ColorPickerReset", R.string.ColorPickerReset), (dialogInterface, i) -> {
                        int differenceUI = OwlConfig.getDifferenceUI();
                        OwlConfig.resetSettings();
                        Theme.lastHolidayCheckTime = 0;
                        Theme.dialogs_holidayDrawable = null;
                        getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                        OwlConfig.doRebuildUIWithDiff(differenceUI, parentLayout);
                        BulletinFactory.of(OwlgramSettings.this).createSimpleBulletin(R.raw.forward, LocaleController.getString("ResetSettingsHint", R.string.ResetSettingsHint)).show();
                    });
                    AlertDialog alertDialog = builder.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.round_settings_backup_restore, LocaleController.getString("ExportSettings", R.string.ExportSettings));
        menuItem.addSubItem(2, R.drawable.round_settings_backup_reset, LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));

    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            OwlConfig.shareSettings(getParentActivity());
        } else if (id == 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            builder.setView(linearLayout);
            builder.setTitle(LocaleController.getString("ThemeResetToDefaultsTitle", R.string.ThemeResetToDefaultsTitle));
            builder.setMessage(LocaleController.getString("ResetSettingsAlert", R.string.ResetSettingsAlert));
            CheckBoxCell cell = new CheckBoxCell(context, 1);
            cell.setBackground(Theme.getSelectorDrawable(false));
            cell.setText(LocaleController.getString("ResetAutoTranslationPreferences", R.string.ResetAutoTranslationPreferences), "", false, false);
            cell.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16) : AndroidUtilities.dp(8), 0, LocaleController.isRTL ? AndroidUtilities.dp(8) : AndroidUtilities.dp(16), 0);
            linearLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            cell.setOnClickListener(v -> cell.setChecked(!cell.isChecked(), true));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.setPositiveButton(LocaleController.getString("Reset", R.string.Reset), (dialogInterface, i) -> {
                int differenceUI = OwlConfig.getDifferenceUI();
                OwlConfig.resetSettings();
                Theme.lastHolidayCheckTime = 0;
                Theme.dialogs_holidayDrawable = null;
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                if (cell.isChecked()) AutoTranslateConfig.resetExceptions();
                OwlConfig.doRebuildUIWithDiff(differenceUI, parentLayout);
                BulletinFactory.of(OwlgramSettings.this).createSimpleBulletin(R.raw.forward, LocaleController.getString("ResetSettingsHint", R.string.ResetSettingsHint)).show();
            });
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
            }
        }
    }

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == channelUpdatesRow) {
                MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("ChannelUsername", R.string.ChannelUsername), this, 1);
            } else if (position == groupUpdatesRow) {
                MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("GroupUsername", R.string.GroupUsername), this, 1);
            } else if (position == sourceCodeRow) {
                Browser.openUrl(getParentActivity(), "https://github.com/OwlGramDev/OwlGram");
            } else if (position == supportTranslationRow) {
                Browser.openUrl(getParentActivity(), "https://translations.owlgram.org/");
            } else if (position == generalSettingsRow) {
                presentFragment(new OwlgramGeneralSettings());
            } else if (position == chatSettingsRow) {
                presentFragment(new OwlgramChatSettings());
            } else if (position == updateSettingsRow) {
                presentFragment(new OwlgramUpdateSettings());
            } else if (position == experimentalSettingsRow) {
                presentFragment(new OwlgramExperimentalSettings());
            } else if (position == supportDonationRow) {
                Browser.openUrl(getParentActivity(), "https://donations.owlgram.org/");
            } else if (position == appearanceSettingsRow) {
                presentFragment(new OwlgramAppearanceSettings());
            } else if (position == bugReportRow) {
                AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
                BulletinFactory.of(OwlgramSettings.this).createCopyBulletin(LocaleController.getString("ReportDetailsCopied", R.string.ReportDetailsCopied)).show();
            }
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        updateSettingsRow = -1;

        categoryHeaderRow = rowCount++;
        generalSettingsRow = rowCount++;
        appearanceSettingsRow = rowCount++;
        chatSettingsRow = rowCount++;
        experimentalSettingsRow = rowCount++;
        if (StoreUtils.isFromCheckableStore() || !StoreUtils.isDownloadedFromAnyStore()) {
            updateSettingsRow = rowCount++;
        }
        divisorInfoRow = rowCount++;
        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        groupUpdatesRow = rowCount++;
        sourceCodeRow = rowCount++;
        supportTranslationRow = rowCount++;
        supportDonationRow = rowCount++;
        bugReportRow = rowCount++;

        if (listAdapter != null) {
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
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == generalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("General", R.string.General), R.drawable.msg_media, true);
                    } else if (position == chatSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Chat", R.string.Chat), R.drawable.msg_msgbubble3, true);
                    } else if (position == updateSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("OwlUpdates", R.string.OwlUpdates), R.drawable.round_update_white_28, false);
                    } else if (position == channelUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("OfficialChannel", R.string.OfficialChannel), "@" + LocaleController.getString("ChannelUsername", R.string.ChannelUsername), R.drawable.msg_channel, true);
                    } else if (position == groupUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("OfficialGroup", R.string.OfficialGroup), "@" + LocaleController.getString("GroupUsername", R.string.GroupUsername), R.drawable.msg_groups, true);
                    } else if (position == experimentalSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Experimental", R.string.Experimental), R.drawable.outline_science_white, true);
                    } else if (position == appearanceSettingsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("Appearance", R.string.Appearance), R.drawable.settings_appearance, true);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == categoryHeaderRow) {
                        headerCell.setText(LocaleController.getString("Settings", R.string.Settings));
                    } else if (position == infoHeaderRow) {
                        headerCell.setText(LocaleController.getString("Info", R.string.Info));
                    }
                    break;
                case 4:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailCell.setMultilineDetail(true);
                    if (position == supportTranslationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("TranslateOwl", R.string.TranslateOwl), LocaleController.getString("TranslateOwlDesc", R.string.TranslateOwlDesc), R.drawable.round_translate_white_28, true);
                    } else if (position == supportDonationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("Donate", R.string.Donate), LocaleController.getString("DonateDesc", R.string.DonateDesc), R.drawable.round_favorite_border_white, true);
                    } else if (position == sourceCodeRow) {
                        String commitInfo = String.format("%s commit, %s", BuildConfig.GIT_COMMIT_HASH, LocaleController.formatDateAudio(BuildConfig.GIT_COMMIT_DATE, false));
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("SourceCode", R.string.SourceCode), commitInfo, R.drawable.outline_source_white_28, true);
                    } else if (position == bugReportRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CopyReportDetails", R.string.CopyReportDetails), LocaleController.getString("CopyReportDetailsDesc", R.string.CopyReportDetailsDesc), R.drawable.bug_report, false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 2 || type == 4;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == divisorInfoRow) {
                return 1;
            } else if (position == generalSettingsRow || position == chatSettingsRow || position == updateSettingsRow ||
                    position == channelUpdatesRow || position == groupUpdatesRow ||
                    position == experimentalSettingsRow || position == appearanceSettingsRow) {
                return 2;
            } else if (position == categoryHeaderRow || position == infoHeaderRow) {
                return 3;
            } else if (position == supportTranslationRow || position == supportDonationRow || position == sourceCodeRow || position == bugReportRow) {
                return 4;
            }
            return 1;
        }
    }
}
