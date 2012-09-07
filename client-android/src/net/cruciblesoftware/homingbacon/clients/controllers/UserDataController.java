package net.cruciblesoftware.homingbacon.clients.controllers;

import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.clients.daos.ServerConnection;
import net.cruciblesoftware.homingbacon.clients.models.AppModel;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;

public class UserDataController implements Message.Listener {
    private static final String TAG = "HB: " + UserDataController.class.getSimpleName();

    private AppModel model;
    private PostOffice post;
    private ServerConnection server;

    public UserDataController() {
        model = AppModel.getInstance();
        post = PostOffice.getInstance();
        post.registerListener(this);
        server = new ServerConnection();
    }

    @Override
    public void receiveMessage(Message msg) {
        String username;
        switch(msg.type) {
        case MODEL_CREATE_USER:
            username = model.getUser().getUsername();
            DebugLog.log(TAG, "creating new user '" + username + "'");
            server.addUser(username, this);
            break;
        case MODEL_UPDATE_USER_USERNAME:
            String oldUsername = msg.data;
            String newUsername = model.getUser().getUsername();
            DebugLog.log(TAG, "TODO: updating server from username '" + oldUsername + "' to username '" + newUsername + "'");
            //server.changeUsername(fromUsername, toUsername, this);
            break;
        case SERVER_RESPONSE:
            username = model.getUser().getUsername();
            String errMsg = JsonUtils.getServerErrorMessage(msg.data);
            if(errMsg != null) {
                DebugLog.err(TAG, "ERROR: creating/updating user '" + username + "': server reports: " + errMsg);
            }
            break;
        default:
            break;
        }
    }

}
