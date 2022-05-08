package it.owlgram.android.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MessageHelper extends BaseController {

    private static final MessageHelper[] Instance = new MessageHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public MessageHelper(int num) {
        super(num);
    }

    public void saveStickerToGallery(Activity activity, MessageObject messageObject, Runnable callback) {
        saveStickerToGallery(activity, getPathToMessage(messageObject), messageObject.isVideoSticker(), callback);
    }

    public static void saveStickerToGallery(Activity activity, TLRPC.Document document, Runnable callback) {
        String path = FileLoader.getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, MessageObject.isVideoSticker(document), callback);
    }

    public MessageObject getMessageForTranslate(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        if (selectedObjectGroup != null && !selectedObjectGroup.isDocuments) {
            messageObject = getTargetMessageObjectFromGroup(selectedObjectGroup);
        } else if (selectedObject.isPoll()) {
            messageObject = selectedObject;
        } else if (!TextUtils.isEmpty(selectedObject.messageOwner.message)) {
            messageObject = selectedObject;
        }
        return messageObject;
    }


    private static void saveStickerToGallery(Activity activity, String path, boolean video, Runnable callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (video) {
                    MediaController.saveFile(path, activity, 1, null, null, callback);
                } else {
                    Bitmap image = BitmapFactory.decodeFile(path);
                    if (image != null) {
                        File file = new File(path.replace(".webp", ".png"));
                        FileOutputStream stream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                        MediaController.saveFile(file.toString(), activity, 0, null, null, callback);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public String getPathToMessage(MessageObject messageObject) {
        String path = messageObject.messageOwner.attachPath;
        if (!TextUtils.isEmpty(path)) {
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getPathToMessage(messageObject.messageOwner).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getPathToAttach(messageObject.getDocument(), true).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                return null;
            }
        }
        return path;
    }

    public static MessageHelper getInstance(int num) {
        MessageHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (MessageHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new MessageHelper(num);
                }
            }
        }
        return localInstance;
    }

    public boolean isMessageObjectAutoTranslatable(MessageObject messageObject) {
        if (messageObject.translated || messageObject.translating || messageObject.isOutOwner()) {
            return false;
        }
        if (messageObject.isPoll()) {
            return true;
        }
        return !TextUtils.isEmpty(messageObject.messageOwner.message) && !isLinkOrEmojiOnlyMessage(messageObject);
    }

    public boolean isLinkOrEmojiOnlyMessage(MessageObject messageObject) {
        var entities = messageObject.messageOwner.entities;
        if (entities != null) {
            for (TLRPC.MessageEntity entity : entities) {
                if (entity instanceof TLRPC.TL_messageEntityBotCommand ||
                        entity instanceof TLRPC.TL_messageEntityEmail ||
                        entity instanceof TLRPC.TL_messageEntityUrl ||
                        entity instanceof TLRPC.TL_messageEntityMention ||
                        entity instanceof TLRPC.TL_messageEntityCashtag ||
                        entity instanceof TLRPC.TL_messageEntityHashtag ||
                        entity instanceof TLRPC.TL_messageEntityBankCard ||
                        entity instanceof TLRPC.TL_messageEntityPhone) {
                    if (entity.offset == 0 && entity.length == messageObject.messageOwner.message.length()) {
                        return true;
                    }
                }
            }
        }
        return isEmoji(messageObject.messageOwner.message);
    }

    private boolean isEmoji(String message) {
        return message.matches("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                "[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
                "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
                "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+");
    }

    public MessageObject getMessageForRepeat(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        if (selectedObjectGroup != null && !selectedObjectGroup.isDocuments) {
            messageObject = getTargetMessageObjectFromGroup(selectedObjectGroup);
        } else if (!TextUtils.isEmpty(selectedObject.messageOwner.message) || selectedObject.isAnyKindOfSticker()) {
            messageObject = selectedObject;
        }
        return messageObject;
    }

    private MessageObject getTargetMessageObjectFromGroup(MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        for (MessageObject object : selectedObjectGroup.messages) {
            if (!TextUtils.isEmpty(object.messageOwner.message)) {
                if (messageObject != null) {
                    messageObject = null;
                    break;
                } else {
                    messageObject = object;
                }
            }
        }
        return messageObject;
    }

    public MessageObject resetMessageContent(long dialogId, MessageObject messageObject, boolean translated) {
        TLRPC.Message message = messageObject.messageOwner;
        MessageObject obj = new MessageObject(currentAccount, message, true, true);
        obj.originalMessage = messageObject.originalMessage;
        obj.originalEntities = messageObject.originalEntities;
        obj.originalReplyMarkupRows = messageObject.originalReplyMarkupRows;
        obj.translating = false;
        obj.translated = translated;
        if (messageObject.isSponsored()) {
            obj.sponsoredId = messageObject.sponsoredId;
            obj.botStartParam = messageObject.botStartParam;
        }
        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialogId, arrayList, false);
        return obj;
    }

    public MessageObject setTranslating(long dialogId, MessageObject messageObject, boolean translating) {
        TLRPC.Message message = messageObject.messageOwner;
        MessageObject obj = new MessageObject(currentAccount, message, true, true);
        obj.translating = translating;
        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialogId, arrayList, false);
        return obj;
    }

    public String getPlainText(MessageObject messageObject) {
        StringBuilder plainText;
        if (messageObject.type == MessageObject.TYPE_POLL) {
            TLRPC.Poll poll = ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll;
            plainText = new StringBuilder(poll.question);
            plainText.append("\n");
            for (int a = 0; a < poll.answers.size(); a++) {
                TLRPC.TL_pollAnswer answer = poll.answers.get(a);
                plainText.append("\n").append("\uD83D\uDD18").append(" ").append(answer.text);
            }
        } else {
            plainText = new StringBuilder(messageObject.messageOwner.message);
            if (messageObject.messageOwner.reply_markup != null) {
                plainText.append("\n");
                for (int a = 0; a < messageObject.messageOwner.reply_markup.rows.size(); a++) {
                    TLRPC.TL_keyboardButtonRow row = messageObject.messageOwner.reply_markup.rows.get(a);
                    for (int b = 0; b < row.buttons.size(); b++) {
                        TLRPC.KeyboardButton button = row.buttons.get(b);
                        plainText.append("\n").append("\uD83D\uDD18").append(" ").append(button.text);
                    }
                }
            }
        }
        return plainText.toString();
    }

    public static class ReplyMarkupButtonsTexts {
        private final ArrayList<ArrayList<String>> texts = new ArrayList<>();

        public ReplyMarkupButtonsTexts(ArrayList<TLRPC.TL_keyboardButtonRow> source) {
            for (int a = 0; a < source.size(); a++) {
                ArrayList<TLRPC.KeyboardButton> buttonRow = source.get(a).buttons;
                ArrayList<String> row = new ArrayList<>();
                for (int b = 0; b < buttonRow.size(); b++) {
                    TLRPC.KeyboardButton button2 = buttonRow.get(b);
                    row.add(button2.text);
                }
                texts.add(row);
            }
        }

        public ArrayList<ArrayList<String>> getTexts() {
            return texts;
        }

        public void applyTextToKeyboard(ArrayList<TLRPC.TL_keyboardButtonRow> rows) {
            for (int a = 0; a < rows.size(); a++) {
                ArrayList<TLRPC.KeyboardButton> buttonRow = rows.get(a).buttons;
                ArrayList<String> row = texts.get(a);
                for (int b = 0; b < buttonRow.size(); b++) {
                    TLRPC.KeyboardButton button2 = buttonRow.get(b);
                    button2.text = row.get(b);
                }
            }
        }
    }
}
