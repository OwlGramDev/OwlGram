package it.owlgram.android.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.BlurIntensityCell;
import it.owlgram.android.components.DrawerProfilePreviewCell;
import it.owlgram.android.components.DynamicButtonSelector;
import it.owlgram.android.components.ThemeSelectorDrawerCell;

public class OwlgramAppearanceSettings extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private DrawerProfilePreviewCell profilePreviewCell;

    private int drawerRow;
    private int drawerAvatarAsBackgroundRow;
    private int showGradientRow;
    private int showAvatarRow;
    private int drawerDarkenBackgroundRow;
    private int drawerBlurBackgroundRow;
    private int drawerDividerRow;
    private int editBlurHeaderRow;
    private int editBlurRow;
    private int editBlurDividerRow;
    private int themeDrawerHeader;
    private int themeDrawerRow;
    private int themeDrawerDividerRow;
    private int menuItemsRow;
    private int dynamicButtonHeaderRow;
    private int dynamicButtonRow;
    private int dynamicDividerRow;
    private int fontsAndEmojiHeaderRow;
    private int useSystemFontRow;
    private int useSystemEmojiRow;
    private int fontsAndEmojiDividerRow;
    private int appearanceHeaderRow;
    private int forcePacmanRow;
    private int showSantaHatRow;
    private int showFallingSnowRow;
    private int messageTimeSwitchRow;
    private int roundedNumberSwitchRow;
    private int smartButtonsRow;
    private int appBarShadowRow;
    private int slidingTitleRow;
    private int searchIconInActionBarRow;
    private int appearanceDividerRow;
    private int showNameInActionBarRow;
    private int showPencilIconRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("Appearance", R.string.Appearance));
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

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == showAvatarRow) {
                OwlConfig.toggleShowAvatarImage();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showAvatarImage);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == showGradientRow) {
                OwlConfig.toggleShowGradientColor();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showGradientColor);
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
            } else if (position == drawerBlurBackgroundRow) {
                OwlConfig.toggleAvatarBackgroundBlur();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.avatarBackgroundBlur);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
                if(OwlConfig.avatarBackgroundBlur) {
                    listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
                } else {
                    listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
                }
                updateRowsId(false);
            } else if (position == drawerAvatarAsBackgroundRow) {
                OwlConfig.toggleAvatarAsDrawerBackground();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.avatarAsDrawerBackground);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());
                if (OwlConfig.avatarAsDrawerBackground) {
                    updateRowsId(false);
                    listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (OwlConfig.avatarBackgroundBlur ? 3:0));
                } else {
                    listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (OwlConfig.avatarBackgroundBlur ? 3:0));
                    updateRowsId(false);
                }
            } else if (position == menuItemsRow) {
                presentFragment(new DrawerOrderSettings());
            } else if (position == useSystemFontRow) {
                OwlConfig.toggleUseSystemFont();
                AndroidUtilities.clearTypefaceCache();
                Parcelable recyclerViewState = null;
                if (listView.getLayoutManager() != null) {
                    recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
                }
                parentLayout.rebuildAllFragmentViews(true, true);
                listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else if (position == useSystemEmojiRow) {
                OwlConfig.toggleUseSystemEmoji();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.useSystemEmoji);
                }
            } else if (position == forcePacmanRow) {
                OwlConfig.togglePacmanForced();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.pacmanForced);
                }
            } else if (position == smartButtonsRow) {
                OwlConfig.toggleSmartButtons();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.smartButtons);
                }
            } else if (position == appBarShadowRow) {
                OwlConfig.toggleAppBarShadow();
                ActionBarLayout.headerShadowDrawable = OwlConfig.disableAppBarShadow ? null : parentLayout.getResources().getDrawable(R.drawable.header_shadow).mutate();
                Parcelable recyclerViewState = null;
                if (listView.getLayoutManager() != null) {
                    recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
                }
                parentLayout.rebuildAllFragmentViews(true, true);
                listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            } else if (position == showSantaHatRow) {
                OwlConfig.toggleShowSantaHat();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showSantaHat);
                }
                Theme.lastHolidayCheckTime = 0;
                Theme.dialogs_holidayDrawable = null;
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == showFallingSnowRow) {
                OwlConfig.toggleShowSnowFalling();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showSnowFalling);
                }
                Theme.lastHolidayCheckTime = 0;
                Theme.dialogs_holidayDrawable = null;
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == slidingTitleRow) {
                OwlConfig.toggleSlidingChatTitle();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.slidingChatTitle);
                }
            } else if (position == messageTimeSwitchRow) {
                OwlConfig.toggleFullTime();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.fullTime);
                }
                LocaleController.getInstance().recreateFormatters();
            } else if (position == roundedNumberSwitchRow) {
                OwlConfig.toggleRoundedNumbers();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.roundedNumbers);
                }
            } else if (position == searchIconInActionBarRow) {
                OwlConfig.toggleSearchIconInActionBar();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.searchIconInActionBar);
                }
            } else if (position == showNameInActionBarRow) {
                OwlConfig.toggleShowNameInActionBar();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showNameInActionBar);
                    parentLayout.rebuildAllFragmentViews(false, false);
                }
            } else if (position == showPencilIconRow) {
                OwlConfig.toggleShowPencilIcon();
                parentLayout.rebuildAllFragmentViews(false, false);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OwlConfig.showPencilIcon);
                }
            }
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        showGradientRow = -1;
        showAvatarRow = -1;
        drawerDarkenBackgroundRow = -1;
        drawerBlurBackgroundRow = -1;
        editBlurHeaderRow = -1;
        editBlurRow = -1;
        editBlurDividerRow = -1;
        showSantaHatRow = -1;
        showFallingSnowRow = -1;

        drawerRow = rowCount++;
        drawerAvatarAsBackgroundRow = rowCount++;
        if (OwlConfig.avatarAsDrawerBackground) {
            showGradientRow = rowCount++;
            showAvatarRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
            drawerBlurBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        if (OwlConfig.avatarBackgroundBlur && OwlConfig.avatarAsDrawerBackground) {
            editBlurHeaderRow = rowCount++;
            editBlurRow = rowCount++;
            editBlurDividerRow = rowCount++;
        }

        themeDrawerHeader = rowCount++;
        themeDrawerRow = rowCount++;
        menuItemsRow = rowCount++;
        themeDrawerDividerRow = rowCount++;

        dynamicButtonHeaderRow = rowCount++;
        dynamicButtonRow = rowCount++;
        dynamicDividerRow = rowCount++;

        fontsAndEmojiHeaderRow = rowCount++;
        useSystemFontRow = rowCount++;
        useSystemEmojiRow = rowCount++;
        fontsAndEmojiDividerRow = rowCount++;

        appearanceHeaderRow = rowCount++;
        forcePacmanRow = rowCount++;
        if (((Theme.getEventType() == 0 && OwlConfig.eventType == 0) || OwlConfig.eventType == 1)) {
            showSantaHatRow = rowCount++;
            showFallingSnowRow = rowCount++;
        }
        messageTimeSwitchRow = rowCount++;
        roundedNumberSwitchRow = rowCount++;
        showPencilIconRow = rowCount++;
        smartButtonsRow = rowCount++;
        appBarShadowRow = rowCount++;
        slidingTitleRow = rowCount++;
        showNameInActionBarRow = rowCount++;
        searchIconInActionBarRow = rowCount++;
        appearanceDividerRow = rowCount++;

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
                    if (position == editBlurHeaderRow) {
                        headerCell.setText(LocaleController.getString("BlurIntensity", R.string.BlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(LocaleController.getString("SideBarIconSet", R.string.SideBarIconSet));
                    } else if (position == dynamicButtonHeaderRow) {
                        headerCell.setText(LocaleController.getString("ButtonShape", R.string.ButtonShape));
                    } else if (position == fontsAndEmojiHeaderRow) {
                        headerCell.setText(LocaleController.getString("FontsAndEmojis", R.string.FontsAndEmojis));
                    } else if (position == appearanceHeaderRow) {
                        headerCell.setText(LocaleController.getString("Appearance", R.string.Appearance));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == showGradientRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShadeBackground", R.string.ShadeBackground), OwlConfig.showGradientColor, true);
                    } else if (position == showAvatarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowAvatar", R.string.ShowAvatar), OwlConfig.showAvatarImage, drawerBlurBackgroundRow != -1);
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarAsBackground", R.string.AvatarAsBackground), OwlConfig.avatarAsDrawerBackground, OwlConfig.avatarAsDrawerBackground);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarBlur", R.string.AvatarBlur), OwlConfig.avatarBackgroundBlur, !OwlConfig.avatarBackgroundBlur);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarDarken", R.string.AvatarDarken), OwlConfig.avatarBackgroundDarken, true);
                    } else if (position == useSystemFontRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("UseSystemFonts", R.string.UseSystemFonts), OwlConfig.useSystemFont, true);
                    } else if (position == useSystemEmojiRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("UseSystemEmojis", R.string.UseSystemEmojis), OwlConfig.useSystemEmoji, true);
                    } else if (position == messageTimeSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("FormatTimeSeconds", R.string.FormatTimeSeconds), LocaleController.getString("FormatTimeSecondsDesc", R.string.FormatTimeSecondsDesc), OwlConfig.fullTime, true, true);
                    } else if (position == roundedNumberSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("NumberRounding", R.string.NumberRounding), LocaleController.getString("NumberRoundingDesc", R.string.NumberRoundingDesc), OwlConfig.roundedNumbers, true, true);
                    } else if (position == forcePacmanRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("PacManAnimation", R.string.PacManAnimation), OwlConfig.pacmanForced, true);
                    } else if (position == smartButtonsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShortcutsForAdmins", R.string.ShortcutsForAdmins), OwlConfig.smartButtons, true);
                    } else if (position == appBarShadowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AppBarShadow", R.string.AppBarShadow), OwlConfig.disableAppBarShadow, true);
                    } else if (position == showSantaHatRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ChristmasHat", R.string.ChristmasHat), OwlConfig.showSantaHat, true);
                    } else if (position == showFallingSnowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("FallingSnow", R.string.FallingSnow), OwlConfig.showSnowFalling, true);
                    } else if (position == slidingTitleRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("SlidingTitle", R.string.SlidingTitle), LocaleController.getString("SlidingTitleDesc", R.string.SlidingTitleDesc), OwlConfig.slidingChatTitle, true, true);
                    } else if (position == searchIconInActionBarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("SearchIconTitleBar", R.string.SearchIconTitleBar), OwlConfig.searchIconInActionBar, true);
                    } else if (position == showNameInActionBarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AccountNameTitleBar", R.string.AccountNameTitleBar), OwlConfig.showNameInActionBar, true);
                    } else if (position == showPencilIconRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowPencilIcon", R.string.ShowPencilIcon), OwlConfig.showPencilIcon, true);
                    }
                    break;
                case 4:
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                case 6:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                    if (position == menuItemsRow) {
                        textCell.setTextAndIcon(LocaleController.getString("MenuItems", R.string.MenuItems), R.drawable.msg_newfilter, false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 6;
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
                    view = profilePreviewCell = new DrawerProfilePreviewCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 5:
                    view = new BlurIntensityCell(mContext) {
                        @Override
                        protected void onBlurIntensityChange(int percentage, boolean layout) {
                            super.onBlurIntensityChange(percentage, layout);
                            OwlConfig.saveBlurIntensity(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensityCell) {
                                BlurIntensityCell cell = (BlurIntensityCell) holder.itemView;
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            listAdapter.notifyItemChanged(drawerRow, new Object());
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                    view = new ThemeSelectorDrawerCell(mContext, OwlConfig.eventType) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            OwlConfig.saveEventType(eventSelected);
                            listAdapter.notifyItemChanged(drawerRow, new Object());
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            updateRowsId(false);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 8:
                    view = new DynamicButtonSelector(mContext) {
                        @Override
                        protected void onSelectionChange() {
                            super.onSelectionChange();
                            parentLayout.rebuildAllFragmentViews(false, false);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
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
            if (position == drawerDividerRow || position == editBlurDividerRow || position == themeDrawerDividerRow ||
                    position == dynamicDividerRow || position == fontsAndEmojiDividerRow || position == appearanceDividerRow){
                return 1;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader || position == dynamicButtonHeaderRow ||
                    position == fontsAndEmojiHeaderRow || position == appearanceHeaderRow) {
                return 2;
            } else if ( position == roundedNumberSwitchRow || position == messageTimeSwitchRow ||
                    position == useSystemFontRow || position == useSystemEmojiRow || position == drawerAvatarAsBackgroundRow ||
                    position == drawerDarkenBackgroundRow || position == drawerBlurBackgroundRow || position == showGradientRow ||
                    position == showAvatarRow || position == forcePacmanRow || position == smartButtonsRow ||
                    position == appBarShadowRow|| position == showSantaHatRow || position == showFallingSnowRow ||
                    position == slidingTitleRow || position == searchIconInActionBarRow || position == showNameInActionBarRow ||
                    position == showPencilIconRow) {
                return 3;
            } else if (position == drawerRow) {
                return 4;
            } else if (position == editBlurRow) {
                return 5;
            } else if (position == menuItemsRow) {
                return 6;
            } else if (position == themeDrawerRow) {
                return 7;
            } else if (position  == dynamicButtonRow) {
                return 8;
            }
            return 1;
        }
    }
}
