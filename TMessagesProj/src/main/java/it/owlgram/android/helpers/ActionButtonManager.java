package it.owlgram.android.helpers;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import it.owlgram.android.OwlConfig;

public class ActionButtonManager {

    private JSONArray data;

    public void reset() {
        data = new JSONArray();
    }
    public void load(
            TLRPC.UserFull userInfo,
            TLRPC.User user,
            long userId,
            boolean isSelf,
            boolean isBot,
            boolean userBlocked,
            long chatId,
            TLRPC.Chat chat,
            TLRPC.ChatFull chatInfo,
            boolean existGroupCall,
            boolean canEdit,
            boolean canSearchMembers,
            int maxMegaGroupCount
    ) {
        if (userInfo != null){
            if (isSelf) {
                data.put("camera");
                data.put("edit_name");
                data.put("logout");
            } else {
                boolean isTgUser = userId == 777000 || userId == 42777;
                data.put("send_message");
                if (isBot || isTgUser){
                    if (isBot && !user.bot_nochats) {
                        data.put("add_bot");
                    }
                } else {
                    data.put("call");
                }

                if (userInfo.video_calls_available){
                    data.put("video_call");
                }

                if (data.length() < 3) {
                    if (!isBot && !TextUtils.isEmpty(user.phone)) {
                        data.put("share_contact");
                    } else if (!TextUtils.isEmpty(user.username)){
                        data.put("share");
                    }
                }

                if (!isTgUser){
                    if (userBlocked){
                        if (isBot){
                            data.put("restart");
                        } else {
                            data.put("unblock");
                        }
                    } else {
                        if (isBot){
                            data.put("stop");
                        } else {
                            data.put("block");
                        }
                    }
                }
            }
        } else if(chatId != 0 && chat != null){
            boolean hasAdminRights = ChatObject.hasAdminRights(chat);
            boolean isCreator = chat.creator;
            boolean canLeave = !chat.creator && !chat.left && !chat.kicked;
            boolean discuss_available = chatInfo != null && chatInfo.linked_chat_id != 0;
            boolean canShare = !TextUtils.isEmpty(chat.username);
            boolean isGroup = !ChatObject.isChannelAndNotMegaGroup(chat);
            boolean canAddUsers = false;
            if (ChatObject.isChannel(chat)) {
                if (chatInfo != null && chat.megagroup && chatInfo.participants != null && !chatInfo.participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(chat) && ChatObject.canAddUsers(chat) && chatInfo.participants_count < maxMegaGroupCount) {
                        canAddUsers = true;
                    }
                }
            } else if (chatInfo != null) {
                if (!(chatInfo.participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(chat) || chat.default_banned_rights == null || !chat.default_banned_rights.invite_users) {
                        canAddUsers = true;
                    }
                }
            }

            if (chat.left && !chat.kicked) {
                data.put("join");
            } else if (canAddUsers) {
                data.put("add_user");
            }

            if (existGroupCall) {
                data.put("join_call");
            }

            if (isGroup) {
                if (data.length() <= 1 && canSearchMembers) {
                    data.put("search");
                }
                if (!canEdit || !hasAdminRights) {
                    data.put("info");
                }
            }

            if (canEdit && hasAdminRights) {
                data.put("edit");
            }

            if ((data.length() + (discuss_available ? 1:0) + (canLeave ? 1:0)) < 4 - (canShare ? 1:0) && !isCreator) {
                data.put("report");
            }

            if ((data.length() + (discuss_available ? 1:0) + (canLeave ? 1:0)) < 4 && canShare) {
                data.put("share");
            }

            if (discuss_available && data.length() < 3 + (canLeave ? 0:1)) {
                if (isGroup) {
                    data.put("open_channel");
                } else {
                    data.put("open_discussion");
                }
            }

            if (canLeave){
                data.put("leave");
            }
        }
    }

    public boolean hasItem(String id) {
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                try {
                    if (data.getString(i).equals(id)) {
                        return true;
                    }
                } catch (JSONException ignored) {}
            }
        }
        return false;
    }

    public int size() {
        return data.length();
    }

    private String getColor(String id) {
        switch (id) {
            case "leave":
            case "stop":
            case "block":
            case "logout":
                return Theme.key_dialogTextRed;
            case "unblock":
            case "restart":
                return Theme.key_wallet_greenText;
        }
        return Theme.key_switch2TrackChecked;
    }

    public ActionButtonInfo getItemAt(int index){
        String id = null;
        try {
            id = data.getString(index);
        } catch (JSONException ignored) { }
        String text = null;
        int icon = -1;
        if (id != null) {
            String color = getColor(id);
            switch (id) {
                case "camera":
                    text = LocaleController.getString("AccDescrProfilePicture", R.string.AccDescrProfilePicture);
                    icon = R.raw.camera_outline;
                    break;
                case "edit_name":
                    text = LocaleController.getString("VoipEditName", R.string.VoipEditName);
                    icon = R.drawable.msg_edit;
                    break;
                case "logout":
                    text = LocaleController.getString("LogOut", R.string.LogOut);
                    icon = R.drawable.msg_leave;
                    break;
                case "send_message":
                    text = LocaleController.getString("Send", R.string.Send);
                    icon = R.drawable.profile_newmsg;
                    break;
                case "video_call":
                    text = LocaleController.getString("VideoCall", R.string.VideoCall);
                    icon = R.drawable.msg_videocall;
                    break;
                case "call":
                    text = LocaleController.getString("Call", R.string.Call);
                    icon = R.drawable.msg_calls;
                    break;
                case "restart":
                    text = LocaleController.getString("BotRestart", R.string.BotRestart);
                    icon = R.drawable.msg_retry;
                    break;
                case "unblock":
                    text = LocaleController.getString("Unblock", R.string.Unblock);
                    icon = R.drawable.msg_block;
                    break;
                case "stop":
                    text = LocaleController.getString("BotStop", R.string.BotStop);
                    icon = R.drawable.msg_block;
                    break;
                case "block":
                    text = LocaleController.getString("BlockContact", R.string.BlockContact);
                    icon = R.drawable.msg_block;
                    break;
                case "add_bot":
                    text = LocaleController.getString("Add", R.string.Add);
                    icon = R.drawable.msg_addbot;
                    break;
                case "share_contact":
                case "share":
                    text = LocaleController.getString("BotShare", R.string.BotShare);
                    icon = R.drawable.msg_share;
                    break;
                case "join":
                    text = LocaleController.getString("VoipChatJoin", R.string.VoipChatJoin);
                    icon = R.drawable.msg_link2;
                    break;
                case "add_user":
                    text = LocaleController.getString("Add", R.string.Add);
                    icon = R.drawable.msg_addcontact;
                    break;
                case "join_call":
                    text = LocaleController.getString("StartVoipChatTitle", R.string.StartVoipChatTitle);
                    icon = R.drawable.msg_voicechat;
                    break;
                case "search":
                    text = LocaleController.getString("Search", R.string.Search);
                    icon = R.drawable.msg_search;
                    break;
                case "edit":
                    text = LocaleController.getString("Edit", R.string.Edit);
                    icon = R.drawable.msg_edit;
                    break;
                case "info":
                    text = LocaleController.getString("Info", R.string.Info);
                    icon = R.drawable.msg_info;
                    break;
                case "report":
                    text = LocaleController.getString("ReportChat", R.string.ReportChat);
                    icon = R.drawable.msg_report;
                    break;
                case "open_channel":
                    text = LocaleController.getString("AccDescrChannel", R.string.AccDescrChannel);
                    icon = R.drawable.msg_channel;
                    break;
                case "open_discussion":
                    text = LocaleController.getString("Discussion", R.string.Discussion);
                    icon = R.drawable.msg_discussion;
                    break;
                case "leave":
                    text = LocaleController.getString("VoipGroupLeave", R.string.VoipGroupLeave);
                    icon = R.drawable.msg_leave;
                    break;
            }
            if (text != null) {
                return new ActionButtonInfo(id, text, icon, color);
            }
        }
        return null;
    }

    public static boolean canShowTopActions(TLRPC.Chat currentChat, boolean editItemVisible, boolean isTopic) {
        return (!canShowShortcuts(currentChat, isTopic) || !editItemVisible) && (OwlConfig.buttonStyleType == 5 || isTopic);
    }

    public static boolean canShowCall(TLRPC.Chat currentChat, boolean isTopic) {
        boolean canEdit;
        if (ChatObject.isChannel(currentChat)) {
            canEdit = !ChatObject.isChannelAndNotMegaGroup(currentChat) || ChatObject.hasAdminRights(currentChat);
        } else {
            canEdit = true;
        }
        return canShowShortcuts(currentChat, isTopic) && canEdit;
    }

    public static boolean canShowButtons(boolean isTopic) {
        return OwlConfig.buttonStyleType == 5 || isTopic;
    }

    public static boolean canShowShortcuts(TLRPC.Chat currentChat, boolean isTopic) {
        return (OwlConfig.smartButtons && (ChatObject.hasAdminRights(currentChat) || currentChat != null && currentChat.megagroup && ChatObject.canChangeChatInfo(currentChat))) && !isTopic;
    }

    public static class ActionButtonInfo {
        public final String id;
        public final String text;
        public final int icon;
        public final String color;

        public ActionButtonInfo(String id, String text, int icon, String color) {
            this.id = id;
            this.text = text;
            this.icon = icon;
            this.color = color;
        }
    }
}
