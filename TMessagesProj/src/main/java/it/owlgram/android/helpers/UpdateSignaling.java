package it.owlgram.android.helpers;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import it.owlgram.android.OwlConfig;
import it.owlgram.android.updates.UpdateManager;

public class UpdateSignaling {
    public static void checkWasUpdated() {
        String oldBuildVersion = OwlConfig.oldBuildVersion;
        int selected_account = UserConfig.selectedAccount;
        if (oldBuildVersion == null) {
            for (int i = 0; i < OwlConfig.getActiveAccounts(); i++) {
                MessagesController messagesController = AccountInstance.getInstance(i).getMessagesController();
                messagesController.loadRemoteFilters(true);
                if (messagesController.suggestedFilters.isEmpty()) {
                    messagesController.loadSuggestedFilters();
                }
            }
        }
        if (oldBuildVersion != null && oldBuildVersion.equals(OwlConfig.currentNotificationVersion())) {
            return;
        }
        UpdateManager.getChangelogs(updateResult -> {
            TLRPC.TL_updateServiceNotification update = new TLRPC.TL_updateServiceNotification();
            update.message = updateResult.text;
            update.entities = updateResult.entities;
            update.media = new TLRPC.TL_messageMediaEmpty();
            update.type = "update";
            update.popup = false;
            update.flags |= 2;
            update.inbox_date = ConnectionsManager.getInstance(selected_account).getCurrentTime();
            ArrayList<TLRPC.Update> updates = new ArrayList<>();
            updates.add(update);
            MessagesController.getInstance(selected_account).processUpdateArray(updates, null, null, false, 0);
            OwlConfig.updateCurrentVersion();
        });
    }
}
