package it.owlgram.android.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;

import it.owlgram.android.OwlConfig;

public class MenuOrderManager {
    private static final Object sync = new Object();

    private static boolean configLoaded;
    private static JSONArray data;
    private static final String[] list_items = new String[] {
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
            "archived_messages"
    };

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            String items = OwlConfig.drawerItems;
            try {
                data = new JSONArray(items);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(data.length() == 0) {
                loadDefaultItems();
            }
            configLoaded = true;
        }
    }

    private static void loadDefaultItems() {
        data.put(list_items[0]);
        data.put(list_items[1]);
        data.put(list_items[2]);
        data.put(list_items[3]);
        data.put(list_items[4]);
        data.put(list_items[5]);
        data.put(list_items[9]);
        data.put(list_items[10]);
        OwlConfig.setDrawerItems(data.toString());
    }

    private static int getArrayPosition(String id) {
        try {
            for (int i = 0;i < data.length(); i++) {
                if(data.getString(i).equals(id)) {
                    return i;
                }
            }
        } catch (JSONException ignored) {}
        return -1;
    }

    public static int getPositionItem(String id, boolean isDefault) {
        int position = getArrayPosition(id);
        if(position == -1 && isDefault) {
            position = 0;
            data.put(id);
            OwlConfig.setDrawerItems(data.toString());
        }
        return position;
    }

    public static void changePosition(int oldPosition, int newPosition) {
        try {
            String data1 = data.getString(newPosition);
            String data2 = data.getString(oldPosition);
            data.put(oldPosition, data1);
            data.put(newPosition, data2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OwlConfig.setDrawerItems(data.toString());
    }

    public static EditableMenuItem getSingleAvailableMenuItem(int position) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) == position) {
                return list.get(i);
            }
        }
        return null;
    }

    public static Boolean isAvailable(String id) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if(getPositionItem(list.get(i).id, list.get(i).isDefault) != -1) {
                if(list.get(i).id.equals(id)) {
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
            if(getPositionItem(list.get(i).id, list.get(i).isDefault) == -1) {
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
            if(!isAv) {
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

    public static ArrayList<EditableMenuItem> getMenuItemsEditable() {
        ArrayList<EditableMenuItem> list = new ArrayList<>();
        list.add(
                new EditableMenuItem(
                        list_items[0],
                        LocaleController.getString("NewGroup", R.string.NewGroup),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[1],
                        LocaleController.getString("Contacts", R.string.Contacts),
                        true
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[2],
                        LocaleController.getString("Calls", R.string.Calls),
                        true
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[3],
                        LocaleController.getString("PeopleNearby", R.string.PeopleNearby),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[4],
                        LocaleController.getString("SavedMessages", R.string.SavedMessages),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[5],
                        LocaleController.getString("Settings", R.string.Settings),
                        true
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[6],
                        LocaleController.getString("OwlgramSettings", R.string.OwlgramSettings),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[7],
                        LocaleController.getString("NewChannel", R.string.NewChannel),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[8],
                        LocaleController.getString("NewSecretChat", R.string.NewSecretChat),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[9],
                        LocaleController.getString("InviteFriends", R.string.InviteFriends),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[10],
                        LocaleController.getString("TelegramFeatures", R.string.TelegramFeatures),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[11],
                        LocaleController.getString("ArchivedChats", R.string.ArchivedChats),
                        false
                )
        );
        return list;
    }

    public static void addItem(String id) {
        if(getArrayPosition(id) == -1) {
            addAsFirst(id);
        }
    }

    private static void addAsFirst(String id) {
        JSONArray result = new JSONArray();
        result.put(id);
        for (int i = 0; i < data.length(); i++) {
            try {
                result.put(data.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        data = result;
        OwlConfig.setDrawerItems(data.toString());
    }

    public static void removeItem(String id) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            try {
                String idTmp = data.getString(i);
                if(!idTmp.equals(id)) {
                    result.put(idTmp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        data = result;
        OwlConfig.setDrawerItems(data.toString());
    }

    public static class EditableMenuItem {
        public final String id;
        public final String text;
        public final boolean isDefault;
        public EditableMenuItem(String menu_id, String menu_text, boolean menu_default) {
            id = menu_id;
            text = menu_text;
            isDefault = menu_default;
        }
    }

}
