package net.cruciblesoftware.homingbacon.clients.controllers;

import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.Position;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;
import android.os.Handler;

import com.google.gson.JsonObject;


public class ListenController implements Message.Listener {
    private static final String TAG = "HB: " + ListenController.class.getSimpleName();
    private static final long LISTEN_DELAY = 5 * 1000;

    private AppModel model;
    private PostOffice post;

    private Handler handler;
    private boolean isListening;
    private ServerConnection server;

    private Runnable listenLoop = new Runnable() {
        @Override
        public void run() {
            if(model.hasUser() && model.hasFriend()) {
                String username = model.getUser().getUsername();
                String friendUsername = model.getFriend().getUsername();
                DebugLog.log(TAG, "requesting location of friend '" + friendUsername + "' from server");
                server.getLocation(username, friendUsername, ListenController.this);
            }
            if(isListening) {
                handler.postDelayed(listenLoop, LISTEN_DELAY);
            }
        }
    };

    public ListenController() {
        model = AppModel.getInstance();
        post = PostOffice.getInstance();
        post.registerListener(this);

        handler = new Handler();
        isListening = false;
        server = new ServerConnection();
    }

    @Override
    public void receiveMessage(Message msg) {
        switch(msg.type) {
        case CONTROL_TOGGLE_LISTEN_ON:
            isListening = true;
            handler.postDelayed(listenLoop, LISTEN_DELAY);
            break;
        case CONTROL_TOGGLE_LISTEN_OFF:
            isListening = false;
            break;
        case MODEL_UPDATE_SELECTED_FRIEND:
            handler.postDelayed(listenLoop, LISTEN_DELAY);
            break;
        case SERVER_RESPONSE:
            // handle server error
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg != null) {
                DebugLog.err(TAG, "ERROR: listening for friend location: server reports: " + errMsg);
                return;
            }

            // parse position data as usual
            JsonObject jsonObj = JsonUtils.getJsonObject(msg.data);
            Position pos = JsonUtils.parsePosition(jsonObj);
            model.setFriendPosition(pos);
            break;
        default:
            break;
        }
    }
}
