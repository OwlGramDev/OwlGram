package it.owlgram.android.settings;

import android.transition.TransitionManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.components.BlurIntensityCell;
import it.owlgram.android.components.DrawerProfilePreviewCell;
import it.owlgram.android.components.DynamicButtonSelector;
import it.owlgram.android.components.ThemeSelectorDrawerCell;

public class OwlgramAppearanceSettings extends BaseSettingsActivity {
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
    private int showPencilIconRow;

    // VIEW TYPES
    private static final int TYPE_PROFILE_PREVIEW = 200;
    private static final int TYPE_BLUR_INTENSITY = 201;
    private static final int TYPE_THEME_SELECTOR = 202;
    private static final int TYPE_DYNAMIC_BUTTON_SELECTOR = 203;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("Appearance", R.string.Appearance);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == showAvatarRow) {
            OwlConfig.toggleShowAvatarImage();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.showAvatarImage);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == showGradientRow) {
            OwlConfig.toggleShowGradientColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.showGradientColor);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerDarkenBackgroundRow) {
            OwlConfig.toggleAvatarBackgroundDarken();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.avatarBackgroundDarken);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerBlurBackgroundRow) {
            OwlConfig.toggleAvatarBackgroundBlur();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.avatarBackgroundBlur);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (OwlConfig.avatarBackgroundBlur) {
                listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
            } else {
                listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
            }
            updateRowsId();
        } else if (position == drawerAvatarAsBackgroundRow) {
            OwlConfig.toggleAvatarAsDrawerBackground();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.avatarAsDrawerBackground);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            TransitionManager.beginDelayedTransition(profilePreviewCell);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (OwlConfig.avatarAsDrawerBackground) {
                updateRowsId();
                listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (OwlConfig.avatarBackgroundBlur ? 3 : 0));
            } else {
                listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (OwlConfig.avatarBackgroundBlur ? 3 : 0));
                updateRowsId();
            }
        } else if (position == menuItemsRow) {
            presentFragment(new DrawerOrderSettings());
        } else if (position == useSystemFontRow) {
            OwlConfig.toggleUseSystemFont();
            AndroidUtilities.clearTypefaceCache();
            parentLayout.rebuildAllFragmentViews(true, true);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.useSystemFont);
            }
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
            ActionBarLayout.headerShadowDrawable = OwlConfig.disableAppBarShadow ? null : parentLayout.getView().getResources().getDrawable(R.drawable.header_shadow).mutate();
            parentLayout.rebuildAllFragmentViews(true, true);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.disableAppBarShadow);
            }
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
        } else if (position == showPencilIconRow) {
            OwlConfig.toggleShowPencilIcon();
            parentLayout.rebuildAllFragmentViews(false, false);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OwlConfig.showPencilIcon);
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
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
        searchIconInActionBarRow = rowCount++;
        appearanceDividerRow = rowCount++;
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
                case TYPE_SWITCH:
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
                    } else if (position == showPencilIconRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowPencilIcon", R.string.ShowPencilIcon), OwlConfig.showPencilIcon, true);
                    }
                    break;
                case TYPE_PROFILE_PREVIEW:
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                case TYPE_TEXT_CELL:
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
            return type == TYPE_SWITCH || type == TYPE_TEXT_CELL;
        }

        @Override
        protected View onCreateViewHolder(int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_PROFILE_PREVIEW:
                    view = profilePreviewCell = new DrawerProfilePreviewCell(context);
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case TYPE_BLUR_INTENSITY:
                    view = new BlurIntensityCell(context) {
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
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_THEME_SELECTOR:
                    view = new ThemeSelectorDrawerCell(context, OwlConfig.eventType) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            OwlConfig.saveEventType(eventSelected);
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            updateRowsId();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_DYNAMIC_BUTTON_SELECTOR:
                    view = new DynamicButtonSelector(context) {
                        @Override
                        protected void onSelectionChange() {
                            super.onSelectionChange();
                            parentLayout.rebuildAllFragmentViews(false, false);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == drawerDividerRow || position == editBlurDividerRow || position == themeDrawerDividerRow ||
                    position == dynamicDividerRow || position == fontsAndEmojiDividerRow || position == appearanceDividerRow) {
                return TYPE_SHADOW;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader || position == dynamicButtonHeaderRow ||
                    position == fontsAndEmojiHeaderRow || position == appearanceHeaderRow) {
                return TYPE_HEADER;
            } else if (position == roundedNumberSwitchRow || position == messageTimeSwitchRow ||
                    position == useSystemFontRow || position == useSystemEmojiRow || position == drawerAvatarAsBackgroundRow ||
                    position == drawerDarkenBackgroundRow || position == drawerBlurBackgroundRow || position == showGradientRow ||
                    position == showAvatarRow || position == forcePacmanRow || position == smartButtonsRow ||
                    position == appBarShadowRow || position == showSantaHatRow || position == showFallingSnowRow ||
                    position == slidingTitleRow || position == searchIconInActionBarRow || position == showPencilIconRow) {
                return TYPE_SWITCH;
            } else if (position == drawerRow) {
                return TYPE_PROFILE_PREVIEW;
            } else if (position == editBlurRow) {
                return TYPE_BLUR_INTENSITY;
            } else if (position == menuItemsRow) {
                return TYPE_TEXT_CELL;
            } else if (position == themeDrawerRow) {
                return TYPE_THEME_SELECTOR;
            } else if (position == dynamicButtonRow) {
                return TYPE_DYNAMIC_BUTTON_SELECTOR;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
