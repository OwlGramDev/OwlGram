package it.owlgram.android.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AlertsCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MessageHelper extends BaseController {

    private static final MessageHelper[] Instance = new MessageHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public MessageHelper(int num) {
        super(num);
    }

    public void saveStickerToGallery(Activity activity, MessageObject messageObject, Runnable callback) {
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
                return;
            }
        }
        saveStickerToGallery(activity, path, callback);
    }

    public static void saveStickerToGallery(Activity activity, TLRPC.Document document, Runnable callback) {
        String path = FileLoader.getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, callback);
    }

    private static void saveStickerToGallery(Activity activity, String path, Runnable callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                Bitmap image;
                if (Build.VERSION.SDK_INT >= 19) {
                    image = BitmapFactory.decodeFile(path);
                } else {
                    RandomAccessFile file = new RandomAccessFile(path, "r");
                    ByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, path.length());
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;
                    Utilities.loadWebpImage(null, buffer, buffer.limit(), bmOptions, true);
                    image = Bitmaps.createBitmap(bmOptions.outWidth, bmOptions.outHeight, Bitmap.Config.ARGB_8888);
                    Utilities.loadWebpImage(image, buffer, buffer.limit(), null, true);
                    file.close();
                }
                if (image != null) {
                    File file = new File(path.replace(".webp", ".png"));
                    FileOutputStream stream = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                    MediaController.saveFile(file.toString(), activity, 0, null, null);
                    AndroidUtilities.runOnUIThread(callback);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public static String saveUriToCache(Uri uri) {
        try {
            InputStream inputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
            String fileName = Integer.MIN_VALUE + "_" + SharedConfig.getLastLocalId();
            File fileDir = FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE);
            final File cacheFile = new File(fileDir, fileName);
            if (inputStream != null) {
                AndroidUtilities.copyFile(inputStream, cacheFile);
                SharedConfig.saveConfig();
                return cacheFile.getAbsolutePath();
            } else {
                return null;
            }
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
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

    public void resetMessageContent(long dialogId, MessageObject messageObject, boolean translated) {
        TLRPC.Message message = messageObject.messageOwner;
        MessageObject obj = new MessageObject(currentAccount, message, true, true);
        obj.originalMessage = messageObject.originalMessage;
        obj.originalEntities = messageObject.originalEntities;
        obj.translated = translated;
        if (messageObject.isSponsored()) {
            obj.sponsoredId = messageObject.sponsoredId;
            obj.botStartParam = messageObject.botStartParam;
        }
        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialogId, arrayList, false);
    }

    public void deleteUserChannelHistoryWithSearch(BaseFragment fragment, final long dialogId) {
        deleteUserChannelHistoryWithSearch(fragment, dialogId, 0, -1);
    }

    public void deleteUserChannelHistoryWithSearch(BaseFragment fragment, final long dialogId, final int offsetId, int lastSize) {
        final TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.peer = getMessagesController().getInputPeer(dialogId);
        if (req.peer == null) {
            return;
        }
        req.limit = 100;
        req.q = "";
        req.offset_id = offsetId;
        req.from_id = MessagesController.getInputPeer(getUserConfig().getCurrentUser());
        req.flags |= 1;
        req.filter = new TLRPC.TL_inputMessagesFilterEmpty();
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                int lastMessageId = offsetId;
                if (response != null) {
                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    int size = res.messages.size();
                    if (size == 0) {
                        return;
                    }
                    FileLog.d("deleteUserChannelHistoryWithSearch size = " + size);
                    ArrayList<Integer> ids = new ArrayList<>();
                    long channelId = 0;
                    for (int a = 0; a < size; a++) {
                        TLRPC.Message message = res.messages.get(a);
                        if (message.id > lastMessageId) {
                            lastMessageId = message.id;
                        }
                        if (message.peer_id.channel_id != 0) {
                            channelId = message.peer_id.channel_id;
                        } else if (message.peer_id.chat_id == 0) {
                            continue;
                        }
                        ids.add(message.id);
                    }
                    getMessagesController().deleteMessages(ids, null, null, -channelId, true, false);
                    if (ids.size() == 0 || (offsetId == lastMessageId && lastSize == ids.size())) {
                        return;
                    }
                    deleteUserChannelHistoryWithSearch(fragment, dialogId, lastMessageId, ids.size());
                }
            } else {
                AlertsCreator.showSimpleAlert(fragment, LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred) + "\n" + error.text);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }
}
