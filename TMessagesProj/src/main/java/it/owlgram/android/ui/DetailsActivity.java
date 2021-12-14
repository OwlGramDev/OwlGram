package it.owlgram.android.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class DetailsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int aboutInfoHeaderRow;
    private int nameUserHeaderRow;
    private int idUserHeaderRow;
    private int usernameRow;
    private int aboutDividerRow;
    private int messageHeaderRow;
    private int messageIdRow;
    private int messageTextRow;
    private int messageForwardsRow;
    private int messageDateRow;
    private int messageDateEditedRow;
    private int messageDividerRow;

    private int forwardMessageHeaderRow;
    private int forwardMessageDateRow;
    private int forwardDividerRow;
    private int forwardUserHeaderRow;
    private int forwardUserNameRow;
    private int forwardUserUsernameRow;
    private int forwardUserIdRow;

    private int repliedMessageHeaderRow;
    private int repliedMessageIdRow;
    private int repliedMessageTextRow;
    private int repliedMessageDateRow;
    private int repliedDividerRow;
    private int repliedUserHeaderRow;
    private int repliedUserNameRow;
    private int repliedUserUsernameRow;
    private int repliedUserIdRow;

    private int groupHeaderRow;
    private int groupNameRow;
    private int groupIdRow;
    private int groupUsernameRow;
    private int groupDividerRow;

    private int fileHeaderRow;
    private int fileNameRow;
    private int filePathRow;
    private int fileMimeType;
    private int fileSizeRow;
    private int fileDuration;
    private int fileDCRow;
    private int fileEmojiRow;
    private int fileDividerRow;

    private String filePath;
    private String fileName;
    private String durationString;
    private String emoji;
    private String mimeType;

    private final MessageObject messageObject;
    private TLRPC.Chat fromChat;
    private TLRPC.User fromUser;
    private TLRPC.User fromForwardedUser;
    private TLRPC.User fromRepliedUser;

    public DetailsActivity(MessageObject messageObject) {
        this.messageObject = messageObject;
        if (messageObject.getChatId() != 0) {
            fromChat = getMessagesController().getChat(messageObject.getChatId());
        }
        if (messageObject.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
            fromUser = getMessagesController().getUser(messageObject.messageOwner.from_id.user_id);
        }
        if(messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.from_id instanceof TLRPC.TL_peerUser){
            fromForwardedUser = getMessagesController().getUser(messageObject.messageOwner.fwd_from.from_id.user_id);
        } else if (messageObject.messageOwner.fwd_from != null && !TextUtils.isEmpty(messageObject.messageOwner.fwd_from.from_name)) {
            fromForwardedUser = new TLRPC.User() {};
            fromForwardedUser.first_name = messageObject.messageOwner.fwd_from.from_name;
        }
        if(messageObject.messageOwner.replyMessage != null && messageObject.messageOwner.replyMessage.from_id instanceof TLRPC.TL_peerUser){
            fromRepliedUser = getMessagesController().getUser(messageObject.messageOwner.replyMessage.from_id.user_id);
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsId();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("OwlgramDevDetails", R.string.OwlgramDevDetails));
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
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemLongClickListener((view, position) -> {
            if (getMessagesController().isChatNoForwards(fromChat)) {
                if(fromChat != null && fromChat.broadcast) {
                    BulletinFactory.of(DetailsActivity.this).createSimpleBulletin(R.raw.ic_ban, LocaleController.getString("ForwardsRestrictedInfoChannel", R.string.ForwardsRestrictedInfoChannel)).show();
                } else {
                    BulletinFactory.of(DetailsActivity.this).createSimpleBulletin(R.raw.ic_ban, LocaleController.getString("ForwardsRestrictedInfoGroup", R.string.ForwardsRestrictedInfoGroup)).show();
                }
            } else {
                TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) view;
                AndroidUtilities.addToClipboard(textDetailCell.getTextView().getText().toString());
                BulletinFactory.of(DetailsActivity.this).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
            }
            return true;
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        aboutInfoHeaderRow = -1;
        nameUserHeaderRow = -1;
        idUserHeaderRow = -1;
        usernameRow = -1;
        aboutDividerRow = -1;
        messageHeaderRow = -1;
        messageIdRow = -1;
        messageTextRow = -1;
        messageForwardsRow = -1;
        messageDateEditedRow = -1;
        messageDateRow = -1;
        messageDividerRow = -1;

        forwardMessageHeaderRow = -1;
        forwardMessageDateRow = -1;
        forwardDividerRow = -1;
        forwardUserHeaderRow = -1;
        forwardUserNameRow = -1;
        forwardUserUsernameRow = -1;
        forwardUserIdRow = -1;

        repliedMessageHeaderRow = -1;
        repliedMessageIdRow = -1;
        repliedMessageTextRow = -1;
        repliedMessageDateRow = -1;
        repliedDividerRow = -1;
        repliedUserHeaderRow = -1;
        repliedUserNameRow = -1;
        repliedUserUsernameRow = -1;
        repliedUserIdRow = -1;

        groupHeaderRow = -1;
        groupNameRow = -1;
        groupIdRow = -1;
        groupUsernameRow = -1;
        groupDividerRow = -1;

        fileHeaderRow = -1;
        fileNameRow = -1;
        fileMimeType = -1;
        filePathRow = -1;
        fileSizeRow = -1;
        fileDuration = -1;
        fileDCRow = -1;
        fileEmojiRow = -1;
        fileDividerRow = -1;

        if((fromChat != null && fromUser != null && fromChat.id != fromUser.id) || (fromChat != null && fromUser == null)) {
            groupHeaderRow = rowCount++;
            groupNameRow = rowCount++;
            if(fromChat.username != null) {
                groupUsernameRow = rowCount++;
            }
            groupIdRow = rowCount++;
            groupDividerRow = rowCount++;
        }

        if(fromUser != null){
            aboutInfoHeaderRow = rowCount++;
            nameUserHeaderRow = rowCount++;
            if(fromUser.username != null) {
                usernameRow = rowCount++;
            }
            idUserHeaderRow = rowCount++;
            aboutDividerRow = rowCount++;
        }
        messageHeaderRow = rowCount++;
        messageIdRow = rowCount++;
        if(messageObject.messageOwner.fwd_from == null && !TextUtils.isEmpty(messageObject.messageOwner.message)){
            messageTextRow = rowCount++;
        }
        if(messageObject.messageOwner.forwards > 0) {
            messageForwardsRow = rowCount++;
        }
        if(messageObject.messageOwner.edit_date != 0) {
            messageDateEditedRow = rowCount++;
        }

        messageDateRow = rowCount++;

        if(messageObject.messageOwner.fwd_from != null){
            messageDividerRow = rowCount++;
            forwardMessageHeaderRow = rowCount++;
            if(!TextUtils.isEmpty(messageObject.messageOwner.message)){
                messageTextRow = rowCount++;
            }
            forwardMessageDateRow = rowCount++;
            forwardDividerRow = rowCount++;
            if(fromForwardedUser != null){
                forwardUserHeaderRow = rowCount++;
                forwardUserNameRow = rowCount++;
                if(fromForwardedUser.id != 0){
                    if(fromForwardedUser.username != null) {
                        forwardUserUsernameRow = rowCount++;
                    }
                    forwardUserIdRow = rowCount++;
                }
            }
        }
        if(messageObject.messageOwner.replyMessage != null) {
            messageDividerRow = rowCount++;
            repliedMessageHeaderRow = rowCount++;
            repliedMessageIdRow = rowCount++;
            if(!TextUtils.isEmpty(messageObject.messageOwner.replyMessage.message)){
                repliedMessageTextRow = rowCount++;
            }
            repliedMessageDateRow = rowCount++;
            repliedDividerRow = rowCount++;
            if(fromRepliedUser != null){
                repliedUserHeaderRow = rowCount++;
                repliedUserNameRow = rowCount++;
                if(fromRepliedUser.username != null) {
                    repliedUserUsernameRow = rowCount++;
                }
                repliedUserIdRow = rowCount++;
            }
        }
        if (messageObject.messageOwner.media != null && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
            fileDividerRow = rowCount++;
            fileHeaderRow = rowCount++;
            if(messageObject.messageOwner.media.document != null) {
                if (TextUtils.isEmpty(messageObject.messageOwner.media.document.file_name)) {
                    for (int a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeFilename) {
                            fileName = messageObject.messageOwner.media.document.attributes.get(a).file_name;
                        }
                    }
                } else {
                    fileName = messageObject.messageOwner.media.document.file_name;
                }
                if (!TextUtils.isEmpty(fileName)) {
                    fileNameRow = rowCount++;
                }
                fileMimeType = rowCount++;
                if(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
                    for (int a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeAudio ||
                                messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeVideo) {
                            int duration = messageObject.messageOwner.media.document.attributes.get(a).duration;
                            durationString = AndroidUtilities.formatShortDuration(duration);
                            fileDuration = rowCount++;
                        } else if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeSticker) {
                            TLRPC.TL_documentAttributeSticker attribute = (TLRPC.TL_documentAttributeSticker) messageObject.messageOwner.media.document.attributes.get(a);
                            Emoji.preloadEmoji(attribute.alt);
                            emoji = attribute.alt;
                            fileEmojiRow = rowCount++;
                        }
                    }
                }
            }


            fileDCRow = rowCount++;
            filePath = messageObject.messageOwner.attachPath;
            if (!TextUtils.isEmpty(filePath)) {
                File temp = new File(filePath);
                if (!temp.exists()) {
                    filePath = null;
                }
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = FileLoader.getPathToMessage(messageObject.messageOwner).toString();
                File temp = new File(filePath);
                if (!temp.exists()) {
                    filePath = null;
                }
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = FileLoader.getPathToAttach(messageObject.getDocument(), true).toString();
                File temp = new File(filePath);
                if (!temp.isFile()) {
                    filePath = null;
                }
            }
            if (filePath != null) {
                filePathRow = rowCount++;
            }
            if (messageObject.getSize() != 0) {
                fileSizeRow = rowCount++;
            }
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
                    if (position == aboutInfoHeaderRow) {
                        headerCell.setText(fromUser.bot ? LocaleController.getString("OwlgramBotInfo", R.string.OwlgramBotInfo):LocaleController.getString("OwlgramUserInfo", R.string.OwlgramUserInfo));
                    } else if (position == messageHeaderRow) {
                        headerCell.setText(LocaleController.getString("Message", R.string.Message));
                    } else if (position == forwardMessageHeaderRow) {
                        headerCell.setText(LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage));
                    } else if (position == forwardUserHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramForwardedFrom", R.string.OwlgramForwardedFrom));
                    } else if (position == repliedMessageHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramRepliedMessage", R.string.OwlgramRepliedMessage));
                    } else if (position == repliedUserHeaderRow) {
                        headerCell.setText(LocaleController.getString("OwlgramInReplyTo", R.string.OwlgramInReplyTo));
                    } else if (position == groupHeaderRow) {
                        headerCell.setText(fromChat != null && fromChat.broadcast ? LocaleController.getString("AccDescrChannel", R.string.AccDescrChannel):LocaleController.getString("AccDescrGroup", R.string.AccDescrGroup));
                    } else if (position == fileHeaderRow) {
                        headerCell.setText(LocaleController.getString("ChatDocument", R.string.ChatDocument));
                    }
                    break;
                case 3:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    if (position == idUserHeaderRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromUser.id), "ID",false);
                    } else if (position == nameUserHeaderRow) {
                        String full_name = fromUser.first_name;
                        if (fromUser.last_name != null){
                            full_name += " " + fromUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAnd21Value(full_name, LocaleController.getString("OwlgramName", R.string.OwlgramName),true);
                    } else if (position == usernameRow) {
                        textDetailCell.setTextAndValue("@" + fromUser.username, LocaleController.getString("Username", R.string.Username),true);
                    } else if (position == messageIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.getId()), "ID", true);
                    } else if (position == messageTextRow) {
                        String message = messageObject.messageText.toString();
                        if (messageObject.caption != null) {
                            message = messageObject.caption.toString();
                        }
                        textDetailCell.setTextWithEmojiAnd21Value(message, LocaleController.getString("OwlgramTextMessage", R.string.OwlgramTextMessage),true);
                    } else if (position == messageDateRow) {
                        long date = (long) messageObject.messageOwner.date * 1000;
                        String title = messageObject.scheduled ?  LocaleController.getString("OwlgramMessageScheduledDate", R.string.OwlgramMessageScheduledDate) : LocaleController.getString("OwlgramMessageDate", R.string.OwlgramMessageDate);
                        textDetailCell.setTextAndValue(messageObject.messageOwner.date == 0x7ffffffe ? LocaleController.getString("OwlgramMessageScheduledWhenOnline", R.string.OwlgramMessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == forwardMessageDateRow) {
                        long date = (long) messageObject.messageOwner.fwd_from.date * 1000;
                        String title = messageObject.scheduled ?  LocaleController.getString("OwlgramMessageScheduledDate", R.string.OwlgramMessageScheduledDate) : LocaleController.getString("OwlgramMessageDate", R.string.OwlgramMessageDate);
                        textDetailCell.setTextAndValue(messageObject.messageOwner.fwd_from.date == 0x7ffffffe ? LocaleController.getString("OwlgramMessageScheduledWhenOnline", R.string.OwlgramMessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == forwardUserNameRow) {
                        String full_name = fromForwardedUser.first_name;
                        if (fromForwardedUser.last_name != null){
                            full_name += " " + fromForwardedUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAnd21Value(full_name, LocaleController.getString("OwlgramName", R.string.OwlgramName),fromForwardedUser.id != 0);
                    } else if (position == forwardUserUsernameRow) {
                        textDetailCell.setTextAndValue("@" + fromForwardedUser.username, LocaleController.getString("Username", R.string.Username),true);
                    } else if (position == forwardUserIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromForwardedUser.id), "ID", false);
                    } else if (position == repliedMessageTextRow) {
                        String message = messageObject.messageOwner.replyMessage.message;
                        textDetailCell.setTextWithEmojiAnd21Value(message, LocaleController.getString("OwlgramTextMessage", R.string.OwlgramTextMessage),true);
                    } else if (position == repliedMessageIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.messageOwner.replyMessage.id), "ID", true);
                    } else if (position == repliedMessageDateRow) {
                        long date = (long) messageObject.messageOwner.replyMessage.date * 1000;
                        String title = messageObject.scheduled ?  LocaleController.getString("OwlgramMessageScheduledDate", R.string.OwlgramMessageScheduledDate) : LocaleController.getString("OwlgramMessageDate", R.string.OwlgramMessageDate);
                        textDetailCell.setTextAndValue(messageObject.messageOwner.replyMessage.date == 0x7ffffffe ? LocaleController.getString("OwlgramMessageScheduledWhenOnline", R.string.OwlgramMessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == repliedUserIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromRepliedUser.id), "ID",false);
                    } else if (position == repliedUserNameRow) {
                        String full_name = fromRepliedUser.first_name;
                        if (fromRepliedUser.last_name != null){
                            full_name += " " + fromRepliedUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAnd21Value(full_name, LocaleController.getString("OwlgramName", R.string.OwlgramName),true);
                    } else if (position == repliedUserUsernameRow) {
                        textDetailCell.setTextAndValue("@" + fromRepliedUser.username, LocaleController.getString("Username", R.string.Username),true);
                    } else if (position == groupNameRow) {
                        textDetailCell.setTextWithEmojiAnd21Value(fromChat.title, LocaleController.getString("GroupName", R.string.GroupName),true);
                    } else if (position == groupUsernameRow) {
                        textDetailCell.setTextAndValue("@" + fromChat.username, LocaleController.getString("Username", R.string.Username),true);
                    } else if (position == groupIdRow) {
                        if(ChatObject.isChannel(fromChat)){
                            textDetailCell.setTextAndValue("-100"+fromChat.id, "ID",false);
                        }else{
                            textDetailCell.setTextAndValue("-"+fromChat.id, "ID",false);
                        }
                    } else if (position == fileNameRow) {
                        textDetailCell.setTextAndValue(fileName, LocaleController.getString("OwlgramFileName", R.string.OwlgramFileName),true);
                    } else if (position == filePathRow) {
                        textDetailCell.setTextAndValue(filePath, LocaleController.getString("OwlgramFilePath", R.string.OwlgramFilePath),true);
                    } else if (position == fileSizeRow) {
                        textDetailCell.setTextAndValue(AndroidUtilities.formatFileSize(messageObject.getSize()), LocaleController.getString("OwlgramFileSize", R.string.OwlgramFileSize),true);
                    } else if (position == fileDCRow) {
                        String DC_NAME = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                        int DC = -1;
                        if (messageObject.messageOwner.media.photo != null && messageObject.messageOwner.media.photo.dc_id > 0) {
                            DC = messageObject.messageOwner.media.photo.dc_id;
                        } else if (messageObject.messageOwner.media.document != null && messageObject.messageOwner.media.document.dc_id > 0) {
                            DC = messageObject.messageOwner.media.document.dc_id;
                        }
                        switch (DC){
                            case 1:
                                DC_NAME = "MIA, Miami FL, USA";
                                break;
                            case 2:
                            case 4:
                                DC_NAME = "AMS, Amsterdam, NL";
                                break;
                            case 5:
                                DC_NAME = "SIN, Singapore, SG";
                                break;
                        }
                        if(DC != -1){
                            DC_NAME = String.format(Locale.ENGLISH, "%s - DC%d", DC_NAME, DC);
                        }
                        textDetailCell.setTextAndValue(DC_NAME, LocaleController.getString("OwlgramFileDC", R.string.OwlgramFileDC),true);
                    } else if (position == messageForwardsRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.messageOwner.forwards), LocaleController.getString("OwlgramForwards", R.string.OwlgramForwards), true);
                    } else if (position == messageDateEditedRow) {
                        long date = (long) messageObject.messageOwner.edit_date * 1000;
                        textDetailCell.setTextAndValue(LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), LocaleController.getString("OwlgramEdited", R.string.OwlgramEdited), true);
                    } else if (position == fileDuration) {
                        textDetailCell.setTextAndValue(durationString, LocaleController.getString("UserRestrictionsDuration", R.string.UserRestrictionsDuration), true);
                    } else if (position == fileEmojiRow) {
                        textDetailCell.setTextWithEmojiAnd21Value(emoji, LocaleController.getString("OwlgramEmojiSticker", R.string.OwlgramEmojiSticker), true);
                    } else if (position == fileMimeType) {
                        textDetailCell.setTextAndValue(messageObject.messageOwner.media.document.mime_type, LocaleController.getString("OwlgramMimeType", R.string.OwlgramMimeType), true);
                    }
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
                    view = new TextDetailSettingsCell(mContext);
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
            if(position == aboutDividerRow || position == messageDividerRow || position == forwardDividerRow ||
                    position == repliedDividerRow || position == groupDividerRow || position == fileDividerRow){
                return 1;
            } else if (position == aboutInfoHeaderRow || position == messageHeaderRow || position == forwardMessageHeaderRow ||
                    position == forwardUserHeaderRow || position == repliedMessageHeaderRow || position == repliedUserHeaderRow ||
                    position == groupHeaderRow || position == fileHeaderRow){
                return 2;
            } else if (position == idUserHeaderRow || position == nameUserHeaderRow || position == usernameRow ||
                    position == messageIdRow || position == messageTextRow || position == messageDateRow ||
                    position == forwardMessageDateRow || position == forwardUserIdRow || position == forwardUserUsernameRow ||
                    position == forwardUserNameRow || position == repliedMessageTextRow || position == repliedMessageDateRow ||
                    position == repliedMessageIdRow || position == repliedUserNameRow || position == repliedUserUsernameRow ||
                    position == repliedUserIdRow || position == groupNameRow || position == groupIdRow || position == groupUsernameRow ||
                    position == fileNameRow || position == filePathRow || position == fileSizeRow || position == fileDCRow ||
                    position == messageForwardsRow || position == messageDateEditedRow || position == fileDuration ||
                    position == fileEmojiRow || position == fileMimeType) {
                return 3;
            }
            return 1;
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
