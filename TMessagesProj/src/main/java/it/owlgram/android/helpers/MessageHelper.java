package it.owlgram.android.helpers;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ColoredImageSpan;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import it.owlgram.android.OwlConfig;

public class MessageHelper extends BaseController {

    private static final MessageHelper[] Instance = new MessageHelper[UserConfig.MAX_ACCOUNT_COUNT];
    private static SpannableStringBuilder arrowSpan;
    public static Drawable arrowDrawable;
    public static SpannableStringBuilder editedSpan;
    public static Drawable editedDrawable;

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

    public boolean isMessageTranslatable(MessageObject messageObject) {
        if (messageObject.isPoll()) {
            return true;
        }
        return !TextUtils.isEmpty(messageObject.messageOwner.message) && !isLinkOrEmojiOnlyMessage(messageObject);
    }

    public boolean isMessageObjectAutoTranslatable(MessageObject messageObject) {
        if (messageObject.translated || messageObject.translating || messageObject.isOutOwner()) {
            return false;
        }
        return isMessageTranslatable(messageObject);
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
        return EntitiesHelper.isEmoji(messageObject.messageOwner.message);
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
        return resetMessageContent(dialogId, messageObject, translated, false);
    }

    public MessageObject resetMessageContent(long dialogId, MessageObject messageObject, boolean translated, boolean canceled) {
        TLRPC.Message message = messageObject.messageOwner;
        MessageObject obj = new MessageObject(currentAccount, message, true, true);
        obj.originalMessage = messageObject.originalMessage;
        obj.originalEntities = messageObject.originalEntities;
        obj.originalReplyMarkupRows = messageObject.originalReplyMarkupRows;
        obj.translating = false;
        obj.translated = translated;
        obj.translatedLanguage = messageObject.translatedLanguage;
        obj.canceledTranslation = canceled;
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

    public static CharSequence createEditedString(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }
        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable), 0, 1, 0);
        }
        spannableStringBuilder
                .append(' ')
                .append(OwlConfig.showPencilIcon ? editedSpan : LocaleController.getString("EditedMessage", R.string.EditedMessage))
                .append(' ')
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public static CharSequence createTranslateString(MessageObject messageObject) {
        if (messageObject.translating) {
            return LocaleController.getString("MessageTranslateProgress", R.string.MessageTranslateProgress) + " " + LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000);
        }
        Pair<String, String> translatedLanguage = messageObject.translatedLanguage;
        if (translatedLanguage == null || translatedLanguage.first == null || translatedLanguage.second == null) {
            return LocaleController.getString("MessageTranslated", R.string.MessageTranslated) + " " + LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000);
        }
        if (arrowDrawable == null) {
            arrowDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.search_arrow)).mutate();
        }
        if (arrowSpan == null) {
            arrowSpan = new SpannableStringBuilder("\u200B");
            arrowSpan.setSpan(new ColoredImageSpan(arrowDrawable), 0, 1, 0);
        }
        Locale from = Locale.forLanguageTag(translatedLanguage.first);
        Locale to = Locale.forLanguageTag(translatedLanguage.second);
        String fromString = !TextUtils.isEmpty(from.getScript()) ? String.valueOf(HtmlCompat.fromHtml(from.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY)) : from.getDisplayName();
        fromString = AndroidUtilities.capitalize(fromString);
        String toString = !TextUtils.isEmpty(to.getScript()) ? String.valueOf(HtmlCompat.fromHtml(to.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY)) : to.getDisplayName();
        toString = AndroidUtilities.capitalize(toString);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder
                .append(fromString)
                .append(' ')
                .append(arrowSpan)
                .append(' ')
                .append(toString)
                .append(' ')
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
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

    public void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            addFileToClipboard(new File(path), callback);
        }
    }

    public static void addFileToClipboard(File file, Runnable callback) {
        try {
            Context context = ApplicationLoader.applicationContext;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "label", uri);
            clipboard.setPrimaryClip(clip);
            callback.run();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean canSendAsDice(String text, ChatActivity parentFragment, long dialog_id) {
        boolean canSendGames = true;
        if (DialogObject.isChatDialog(dialog_id)) {
            TLRPC.Chat chat = parentFragment.getMessagesController().getChat(-dialog_id);
            canSendGames = ChatObject.canSendStickers(chat);
        }
        return canSendGames && parentFragment.getMessagesController().diceEmojies.contains(text.replace("\ufe0f", ""));
    }
}
