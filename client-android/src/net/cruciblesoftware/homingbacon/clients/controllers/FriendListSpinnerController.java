package net.cruciblesoftware.homingbacon.clients.controllers;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.os.Handler;

import com.google.gson.JsonObject;

public class FriendListSpinnerController implements Message.Listener {
    private static final String TAG = "HB: " + FriendListSpinnerController.class.getSimpleName();
    private static final long SPINNER_UPDATE_DELAY = 30 * 1000;

    private AppModel model;
    private PostOffice post;

    private Handler handler;
    private boolean isUpdatingList;
    private ServerConnection server;

    private Runnable listUpdateLoop = new Runnable() {
        @Override
        public void run() {
            if(model.hasUser()) {
                String username = model.getUser().getUsername();
                DebugLog.log(TAG, "getting friend list from server");
                server.getFriends(username, FriendListSpinnerController.this);
            }
            if(isUpdatingList) {
                handler.postDelayed(listUpdateLoop, SPINNER_UPDATE_DELAY);
            }
        }
    };

    public FriendListSpinnerController() {
        model = AppModel.getInstance();
        post = PostOffice.getInstance();
        post.registerListener(this);
        isUpdatingList = false;
        handler = new Handler();
        server = new ServerConnection();
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case CONTROL_SPINNER_FRIEND_CHOSEN:
            DebugLog.log(TAG, "chose friend '" + msg.data);
            model.changeFriend(msg.data);
            break;
        case MODEL_CREATE_USER:
            isUpdatingList = true;
            handler.post(listUpdateLoop);
            break;
        case ON_RESUME:
            isUpdatingList = true;
            handler.post(listUpdateLoop);
            break;
        case ON_PAUSE:
            isUpdatingList = false;
            break;
        case SERVER_RESPONSE:
            DebugLog.log(TAG, "message back from server: " + msg.data);
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg == null) {
                JsonObject jsonObj = JsonUtils.getJsonObject(msg.data);
                String oldList = model.getFriendList();
                String newList = jsonObj.get(JsonKeys.FRIEND_LIST).getAsString();
                if(!oldList.equals(newList)) {
                    model.setFriendList(newList);
                }
            } else {
                DebugLog.err(TAG, "ERROR: checking friend list: server reports: " + errMsg);
            }
            break;
        default:
            break;
        }
    }
}
