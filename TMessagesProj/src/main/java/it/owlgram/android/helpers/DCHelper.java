package it.owlgram.android.helpers;

import android.graphics.Color;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class DCHelper {
    public static TInfo getTInfo(TLRPC.UserFull userInfo, TLRPC.ChatFull chatInfo, TLRPC.Chat currentChat, int myDC) {
        int DC = 0;
        String id = "0";
        if(userInfo != null){
            if (UserObject.isUserSelf(userInfo.user)) {
                DC = myDC;
            } else {
                DC = userInfo.profile_photo != null ? userInfo.profile_photo.dc_id:-1;
            }
            id = String.valueOf(userInfo.id);
        }else if(chatInfo != null){
            DC = chatInfo.chat_photo != null ? chatInfo.chat_photo.dc_id:-1;
            if(ChatObject.isChannel(currentChat)){
                id = "-100"+chatInfo.id;
            }else{
                id = "-"+chatInfo.id;
            }
        }
        DC = DC != 0 ? DC:-1;
        return new TInfo(DC, Long.parseLong(id));
    }

    public static class TInfo {
        public final int dcID;
        public final long userID;

        TInfo(int dcID, long userID) {
            this.dcID = dcID;
            this.userID = userID;
        }
    }

    public static int getDCColor(int dc_id) {
        switch (dc_id){
            case 1:
                return 0xFF329AFE;
            case 2:
                return 0xFF8B31FD;
            case 3:
                return 0xFFDA5653;
            case 4:
                return 0xFFF7B139;
            case 5:
                return 0xFF4BD199;
            default:
                return Color.TRANSPARENT;
        }
    }

    public static String getDCIp(int dc_id) {
        switch (dc_id){
            case 1:
                return "149.154.175.50";
            case 2:
                return "149.154.167.50";
            case 3:
                return "149.154.175.100";
            case 4:
                return "149.154.167.91";
            case 5:
                return "91.108.56.100";
            default:
                return "?.?.?.?";
        }
    }

    public static String getDcName(int dc_id) {
        switch (dc_id){
            case 1:
            case 3:
                return "MIA, Miami FL, USA";
            case 2:
            case 4:
                return "AMS, Amsterdam, NL";
            case 5:
                return "SIN, Singapore, SG";
            default:
                return LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
        }
    }

    public static int getDcIcon(int dc_id) {
        switch (dc_id){
            case 1:
                return R.drawable.ic_pluto_datacenter;
            case 2:
                return R.drawable.ic_venus_datacenter;
            case 3:
                return R.drawable.ic_aurora_datacenter;
            case 4:
                return R.drawable.ic_vesta_datacenter;
            case 5:
                return R.drawable.ic_flora_datacenter;
            default:
                return R.drawable.menu_secret_hw;
        }
    }

    public static int getDcIconLittle(int dc_id) {
        switch (dc_id){
            case 1:
                return R.drawable.ic_pluto_datacenter_little;
            case 2:
                return R.drawable.ic_venus_datacenter_little;
            case 3:
                return R.drawable.ic_aurora_datacenter_little;
            case 4:
                return R.drawable.ic_vesta_datacenter_little;
            case 5:
                return R.drawable.ic_flora_datacenter_little;
            default:
                return R.drawable.menu_secret_hw;
        }
    }

    public static class DatacenterStatusChecker {
        private UpdateCallback updateCallback;
        private boolean isRunning = false;
        private boolean doneStopRunning = true;
        private Thread thread;

        public void setOnUpdate(UpdateCallback updateCallback) {
            this.updateCallback = updateCallback;
        }

        public void runListener() {
            if (isRunning) return;
            isRunning = true;
            if (!doneStopRunning) return;
            thread = new Thread() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            String url = "https://app.owlgram.org/dc_status";
                            JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                            JSONArray listDatacenters = obj.getJSONArray("status");
                            int refreshTimeIn = obj.getInt("refresh_in_time");
                            DatacenterList infoArrayList = new DatacenterList();
                            for (int i = 0; i < listDatacenters.length(); i++) {
                                JSONObject dcInfo = listDatacenters.getJSONObject(i);
                                int dcID = dcInfo.getInt("dc_id");
                                int status = dcInfo.getInt("dc_status");
                                int ping = StandardHTTPRequest.ping(DCHelper.getDCIp(dcID));
                                infoArrayList.add(new DatacenterInfo(dcID, status, ping));
                                SystemClock.sleep(25);
                            }
                            if (updateCallback != null) {
                                AndroidUtilities.runOnUIThread(() -> updateCallback.onUpdate(infoArrayList));
                            }
                            SystemClock.sleep(1000L * refreshTimeIn);
                        } catch (Exception ignored) {
                            SystemClock.sleep(1000L);
                        }
                    }
                    doneStopRunning = true;
                }
            };
            thread.start();
        }

        public void stop(boolean forced) {
            isRunning = false;
            doneStopRunning = forced;
            if (forced) {
                thread.interrupt();
            }
        }

        public interface UpdateCallback {
            void onUpdate(DatacenterList result);
        }
    }

    public static class DatacenterList extends ArrayList<DatacenterInfo> {

        public DatacenterInfo getByDc(int dcID) {
            for (int i = 0; i < size(); i++) {
                DatacenterInfo datacenterInfo = get(i);
                if (datacenterInfo.dcID == dcID) return datacenterInfo;
            }
            return null;
        }
    }


    public static class DatacenterInfo {
        public final int dcID, status, ping;

        DatacenterInfo(int dcID, int status, int ping) {
            this.dcID = dcID;
            this.status = status;
            this.ping = ping;
        }
    }
}
