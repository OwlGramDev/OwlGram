package it.owlgram.android;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;

public class MenuOrderController {
    private static final Object sync = new Object();

    private static boolean configLoaded;
    public static final String DIVIDER_ITEM = "divider";
    public static final String[] list_items = new String[]{
            "new_group",
            "contacts",
            "calls",
            "nearby_people",
            "saved_message",
            "settings",
            "owlgram_settings",
            "new_channel",
            "new_secret_chat",
            "invite_friends",
            "telegram_features",
            "archived_messages",
            "datacenter_status",
            "qr_login",
            "set_status",
            "connected_devices",
            "power_usage",
            "proxy_settings",
    };

    static {
        loadConfig();
    }

    public static void reloadConfig() {
        configLoaded = false;
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            if (OwlConfig.drawerItems.size() == 0) {
                loadDefaultItems();
            }
            configLoaded = true;
        }
    }

    private static String[] getDefaultItems() {
        return new String[]{
                list_items[14],
                DIVIDER_ITEM,
                list_items[0],
                list_items[1],
                list_items[2],
                list_items[3],
                list_items[4],
                list_items[5],
                DIVIDER_ITEM,
                list_items[9],
                list_items[10],
        };
    }

    public static void resetToDefaultPosition() {
        OwlConfig.drawerItems.clear();
        loadDefaultItems();
    }

    public static boolean IsDefaultPosition() {
        String[] defaultItems = getDefaultItems();
        int sizeAvailable = sizeAvailable();
        int foundSameItems = 0;
        if (defaultItems.length == sizeAvailable) {
            for (int i = 0; i < sizeAvailable; i++) {
                EditableMenuItem editableMenuItem = MenuOrderController.getSingleAvailableMenuItem(i);
                if (editableMenuItem != null && defaultItems[i].equals(editableMenuItem.id)) {
                    foundSameItems++;
                }
            }
        }
        return sizeAvailable == foundSameItems;
    }

    private static void loadDefaultItems() {
        String[] defaultItems = getDefaultItems();
        for (String defaultItem : defaultItems) {
            OwlConfig.drawerItems.add(defaultItem);
        }
        OwlConfig.applyDrawerItems();
    }

    private static int getArrayPosition(String id) {
        return getArrayPosition(id, 0);
    }

    private static int getArrayPosition(String id, int startFrom) {
        for (int i = startFrom; i < OwlConfig.drawerItems.size(); i++) {
            if (OwlConfig.drawerItems.get(i).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static int getPositionItem(String id, boolean isDefault) {
        return getPositionItem(id, isDefault, 0);
    }

    public static int getPositionItem(String id, boolean isDefault, int startFrom) {
        int position = getArrayPosition(id, startFrom);
        if (position == -1 && isDefault) {
            position = 0;
            OwlConfig.drawerItems.add(id);
            OwlConfig.applyDrawerItems();
        }
        return position;
    }

    public static void changePosition(int oldPosition, int newPosition) {
        OwlConfig.drawerItems.move(oldPosition, newPosition);
        OwlConfig.applyDrawerItems();
    }

    public static EditableMenuItem getSingleAvailableMenuItem(int position) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault, position) == position) {
                return list.get(i);
            }
        }
        return null;
    }

    public static Boolean isAvailable(String id) {
        return isAvailable(id, 0);
    }

    public static Boolean isAvailable(String id, int startFrom) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault, startFrom) != -1) {
                if (list.get(i).id.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static EditableMenuItem getSingleNotAvailableMenuItem(int position) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int curr_pos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) == -1) {
                curr_pos++;
            }
            if (curr_pos == position) {
                return list.get(i);
            }
        }
        return null;
    }

    public static int sizeHints() {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) == -1) {
                size++;
            }
        }
        return size;
    }

    public static int sizeAvailable() {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) != -1) {
                size++;
            }
        }
        return size;
    }

    public static int getPositionOf(String id) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int sizeNAv = 0;
        for (int i = 0; i < list.size(); i++) {
            boolean isAv = getPositionItem(list.get(i).id, list.get(i).isDefault) != -1;
            if (list.get(i).id.equals(id) && !isAv) {
                return sizeNAv;
            }
            if (!isAv) {
                sizeNAv++;
            }
        }
        for (int i = 0; i < sizeAvailable(); i++) {
            EditableMenuItem editableMenuItem = getSingleAvailableMenuItem(i);
            if (editableMenuItem != null && editableMenuItem.id.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static String getText(String id) {
        switch (id) {
            case "new_group":
                return LocaleController.getString("NewGroup", R.string.NewGroup);
            case "contacts":
                return LocaleController.getString("Contacts", R.string.Contacts);
            case "calls":
                return LocaleController.getString("Calls", R.string.Calls);
            case "nearby_people":
                return LocaleController.getString("PeopleNearby", R.string.PeopleNearby);
            case "saved_message":
                return LocaleController.getString("SavedMessages", R.string.SavedMessages);
            case "settings":
                return LocaleController.getString("Settings", R.string.Settings);
            case "owlgram_settings":
                return LocaleController.getString("OwlSetting", R.string.OwlSetting);
            case "new_channel":
                return LocaleController.getString("NewChannel", R.string.NewChannel);
            case "new_secret_chat":
                return LocaleController.getString("NewSecretChat", R.string.NewSecretChat);
            case "invite_friends":
                return LocaleController.getString("InviteFriends", R.string.InviteFriends);
            case "telegram_features":
                return LocaleController.getString("TelegramFeatures", R.string.TelegramFeatures);
            case "archived_messages":
                return LocaleController.getString("ArchivedChats", R.string.ArchivedChats);
            case "datacenter_status":
                return LocaleController.getString("DatacenterStatus", R.string.DatacenterStatus);
            case "qr_login":
                return LocaleController.getString("AuthAnotherClient", R.string.AuthAnotherClient);
            case "set_status":
                return LocaleController.getString("SetEmojiStatus", R.string.SetEmojiStatus);
            case "connected_devices":
                return LocaleController.getString("Devices", R.string.Devices);
            case "power_usage":
                return LocaleController.getString("PowerUsage", R.string.PowerUsage);
            case "proxy_settings":
                return LocaleController.getString("ProxySettings", R.string.ProxySettings);
        }
        throw new RuntimeException("Unknown id: " + id);
    }

    public static ArrayList<EditableMenuItem> getMenuItemsEditable() {
        ArrayList<EditableMenuItem> list = new ArrayList<>();
        for (String id : list_items) {
            list.add(
                    new EditableMenuItem(
                            id,
                            getText(id),
                            "settings".equals(id),
                            "set_status".equals(id)
                    )
            );
        }
        for (String id : OwlConfig.drawerItems) {
            if (id.equals(DIVIDER_ITEM)) {
                list.add(
                        new EditableMenuItem(
                                DIVIDER_ITEM,
                                LocaleController.getString("Divider", R.string.Divider)
                        )
                );
            }
        }
        return list;
    }

    public static void addItem(String id) {
        if (getArrayPosition(id) == -1 || id.equals(DIVIDER_ITEM)) {
            addAsFirst(id);
        }
    }

    private static void addAsFirst(String id) {
        OwlConfig.drawerItems.add(0, id);
        OwlConfig.applyDrawerItems();
    }

    public static void removeItem(int position) {
        OwlConfig.drawerItems.remove(position);
        OwlConfig.applyDrawerItems();
    }

    public static class EditableMenuItem {
        public final String id;
        public final String text;
        public final boolean isDefault;
        public final boolean isPremium;

        public EditableMenuItem(String menu_id, String menu_text) {
            this(menu_id, menu_text, false, false);
        }

        public EditableMenuItem(String menu_id, String menu_text, boolean menu_default, boolean is_premium) {
            id = menu_id;
            text = menu_text;
            isDefault = menu_default;
            isPremium = is_premium;
        }
    }
}
