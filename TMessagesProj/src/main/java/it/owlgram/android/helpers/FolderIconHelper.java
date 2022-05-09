package it.owlgram.android.helpers;


import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;

import java.util.LinkedHashMap;

import it.owlgram.android.OwlConfig;


public class FolderIconHelper {
    public static LinkedHashMap<String, FolderIcon> folderIcons = new LinkedHashMap<>();

    static {
        folderIcons.put("\uD83D\uDC31", FolderIcon.obtain(R.drawable.filter_cat));
        folderIcons.put("\uD83D\uDCD5", FolderIcon.obtain(R.drawable.filter_book));
        folderIcons.put("\uD83D\uDCB0", FolderIcon.obtain(R.drawable.filter_money));
        folderIcons.put("\uD83C\uDFAE", FolderIcon.obtain(R.drawable.filter_game));
        folderIcons.put("\uD83D\uDCA1", FolderIcon.obtain(R.drawable.filter_light));
        folderIcons.put("", FolderIcon.obtain(R.drawable.filter_like));
        folderIcons.put("\uD83C\uDFB5", FolderIcon.obtain(R.drawable.filter_note));
        folderIcons.put("\uD83C\uDFA8", FolderIcon.obtain(R.drawable.filter_palette));
        folderIcons.put("\u2708", FolderIcon.obtain(R.drawable.filter_travel));
        folderIcons.put("\u26BD", FolderIcon.obtain(R.drawable.filter_sport));
        folderIcons.put("\u2B50", FolderIcon.obtain(R.drawable.filter_favorite));
        folderIcons.put("\uD83C\uDF93", FolderIcon.obtain(R.drawable.filter_study));
        folderIcons.put("\uD83D\uDEEB", FolderIcon.obtain(R.drawable.filter_airplane));
        folderIcons.put("\uD83D\uDC64", FolderIcon.obtain(R.drawable.filter_private));
        folderIcons.put("\uD83D\uDC65", FolderIcon.obtain(R.drawable.filter_groups));
        folderIcons.put("\uD83D\uDCAC", FolderIcon.obtain(R.drawable.filter_all));
        folderIcons.put("\u2705", FolderIcon.obtain(R.drawable.filter_unread));
        folderIcons.put("\uD83E\uDD16", FolderIcon.obtain(R.drawable.filter_bot));
        folderIcons.put("\uD83D\uDC51", FolderIcon.obtain(R.drawable.filter_crown));
        folderIcons.put("\uD83C\uDF39", FolderIcon.obtain(R.drawable.filter_flower));
        folderIcons.put("\uD83C\uDFE0", FolderIcon.obtain(R.drawable.filter_home));
        folderIcons.put("\u2764", FolderIcon.obtain(R.drawable.filter_love));
        folderIcons.put("\uD83C\uDFAD", FolderIcon.obtain(R.drawable.filter_mask));
        folderIcons.put("\uD83C\uDF78", FolderIcon.obtain(R.drawable.filter_party));
        folderIcons.put("\uD83D\uDCC8", FolderIcon.obtain(R.drawable.filter_trade));
        folderIcons.put("\uD83D\uDCBC", FolderIcon.obtain(R.drawable.filter_work));
        folderIcons.put("\uD83D\uDD14", FolderIcon.obtain(R.drawable.filter_unmuted));
        folderIcons.put("\uD83D\uDCE2", FolderIcon.obtain(R.drawable.filter_channel));
        folderIcons.put("\uD83D\uDCC1", FolderIcon.obtain(R.drawable.filter_custom));
        folderIcons.put("\uD83D\uDCCB", FolderIcon.obtain(R.drawable.filter_setup));
    }

    public static class FolderIcon {
        public int icon;

        public static FolderIcon obtain(int icon) {
            var folderIcon = new FolderIcon();
            folderIcon.icon = icon;
            return folderIcon;
        }
    }

    public static int getIconWidth() {
        return AndroidUtilities.dp(28);
    }

    public static int getPadding() {
        if (OwlConfig.tabMode == OwlConfig.TAB_TYPE_MIX) {
            return AndroidUtilities.dp(6);
        }
        return 0;
    }

    public static int getTotalIconWidth() {
        int result = 0;
        if (OwlConfig.tabMode != OwlConfig.TAB_TYPE_TEXT) {
            result = getIconWidth() + getPadding();
        }
        return result;
    }

    public static int getPaddingTab() {
        if (OwlConfig.tabMode != OwlConfig.TAB_TYPE_ICON) {
            return AndroidUtilities.dp(32);
        }
        return AndroidUtilities.dp(16);
    }

    public static int getTabIcon(String emoji) {
        if (emoji != null) {
            var folderIcon = folderIcons.get(emoji);
            if (folderIcon != null) {
                return folderIcon.icon;
            }
        }
        return R.drawable.filter_custom;
    }

    public static String[] getEmoticonData(int newFilterFlags) {
        int flags = newFilterFlags & MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS;
        String newName = "";
        String newEmoticon = "";
        if ((flags & MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS) == MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS) {
            if ((newFilterFlags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) != 0) {
                newName = LocaleController.getString("FilterNameUnread", R.string.FilterNameUnread);
                newEmoticon = "\u2705";
            } else if ((newFilterFlags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_MUTED) != 0) {
                newName = LocaleController.getString("FilterNameNonMuted", R.string.FilterNameNonMuted);
                newEmoticon = "\uD83D\uDD14";
            }
        } else if ((flags & MessagesController.DIALOG_FILTER_FLAG_CONTACTS) != 0) {
            flags &=~ MessagesController.DIALOG_FILTER_FLAG_CONTACTS;
            if (flags == 0) {
                newName = LocaleController.getString("FilterContacts", R.string.FilterContacts);
                newEmoticon = "\uD83D\uDC64";
            }
        } else if ((flags & MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS) != 0) {
            flags &=~ MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS;
            if (flags == 0) {
                newName = LocaleController.getString("FilterNonContacts", R.string.FilterNonContacts);
                newEmoticon = "\uD83D\uDC64";
            }
        } else if ((flags & MessagesController.DIALOG_FILTER_FLAG_GROUPS) != 0) {
            flags &=~ MessagesController.DIALOG_FILTER_FLAG_GROUPS;
            if (flags == 0) {
                newName = LocaleController.getString("FilterGroups", R.string.FilterGroups);
                newEmoticon = "\uD83D\uDC65";
            }
        } else if ((flags & MessagesController.DIALOG_FILTER_FLAG_BOTS) != 0) {
            flags &=~ MessagesController.DIALOG_FILTER_FLAG_BOTS;
            if (flags == 0) {
                newName = LocaleController.getString("FilterBots", R.string.FilterBots);
                newEmoticon = "\uD83E\uDD16";
            }
        } else if ((flags & MessagesController.DIALOG_FILTER_FLAG_CHANNELS) != 0) {
            flags &=~ MessagesController.DIALOG_FILTER_FLAG_CHANNELS;
            if (flags == 0) {
                newName = LocaleController.getString("FilterChannels", R.string.FilterChannels);
                newEmoticon = "\uD83D\uDCE2";
            }
        }
        return new String[] {newName, newEmoticon};
    }
}