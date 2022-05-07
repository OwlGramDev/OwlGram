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

    public boolean isMessageObjectTranslatable(MessageObject messageObject) {
        return !messageObject.isAnimatedEmoji() && !TextUtils.isEmpty(messageObject.messageOwner.message) || messageObject.isPoll();
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
