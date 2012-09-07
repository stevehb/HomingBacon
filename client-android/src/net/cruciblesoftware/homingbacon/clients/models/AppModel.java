package net.cruciblesoftware.homingbacon.clients.models;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.Position;
import net.cruciblesoftware.homingbacon.clients.pojos.Message;
import net.cruciblesoftware.homingbacon.clients.pojos.UserPosition;
import net.cruciblesoftware.homingbacon.clients.utils.DebugLog;
import net.cruciblesoftware.homingbacon.clients.utils.PostOffice;

import com.google.gson.JsonObject;

public class AppModel {
    private static final String TAG = "HB: " + AppModel.class.getSimpleName();

    // the data modeled
    private UserPosition user;
    private UserPosition friend;
    private String friendList;
    private boolean hasUser;
    private boolean hasFriend;
    private boolean isListening;
    private boolean isTransmitting;

    private PostOffice post;
    private static AppModel instance;

    private AppModel() {
        DebugLog.log(TAG, "creating the AppModel");
        post = PostOffice.getInstance();
        friendList = "";
        friend = null; //new UserPosition();
        user = null; //new UserPosition();
        hasUser = false;
        hasFriend = false;
        isListening = false;
        isTransmitting = false;
    }

    public static AppModel getInstance() {
        if(instance == null) {
            instance = new AppModel();
        }
        return instance;
    }

    public boolean isListening() {
        return isListening;
    }

    public void setListening(boolean isListening) {
        this.isListening = isListening;
    }

    public boolean isTransmitting() {
        return isListening;
    }

    public void setTransmitting(boolean isTransmitting) {
        this.isTransmitting = isTransmitting;
    }

    public boolean hasUser() {
        return hasUser;
    }

    public boolean hasFriend() {
        return hasFriend;
    }

    public String getFriendList() {
        return friendList;
    }

    public UserPosition getUser() {
        return user;
    }

    public UserPosition getFriend() {
        return friend;
    }

    public synchronized void setFriendList(String friends) {
        friendList = friends;
        post.dispatchMessage(new Message(Message.Type.MODEL_UPDATE_FRIEND_LIST));
    }

    public synchronized void setUsername(String newUsername) {
        if(hasUser()) {
            String oldUsername = user.getUsername();
            user.setUsername(newUsername);
            post.dispatchMessage(new Message(Message.Type.MODEL_UPDATE_USER_USERNAME, oldUsername));
        } else {
            user = new UserPosition();
            user.setUsername(newUsername);
            hasUser = true;
            post.dispatchMessage(new Message(Message.Type.MODEL_CREATE_USER));
        }
    }

    public void setUserPosition(Position pos) {
        setUserPosition(pos.getLatitude(), pos.getLongitude(), pos.getAccuracy(), pos.getEpochTime());
    }

    public synchronized void setUserPosition(double latitude, double longitude, double accuracy, long epochTime) {
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setAccuracy(accuracy);
        user.setEpochTime(epochTime);
        DebugLog.log(TAG, "set new user position, sending update message");
        post.dispatchMessage(new Message(Message.Type.MODEL_UPDATE_USER_POSITION));
    }

    public void setFriendPosition(Position pos) {
        setFriendPosition(pos.getLatitude(), pos.getLongitude(), pos.getAccuracy(), pos.getEpochTime());
    }

    public synchronized void setFriendPosition(double latitude, double longitude, double accuracy, long epochTime) {
        friend.setLatitude(latitude);
        friend.setLongitude(longitude);
        friend.setAccuracy(accuracy);
        friend.setEpochTime(epochTime);
        post.dispatchMessage(new Message(Message.Type.MODEL_UPDATE_FRIEND_POSITION));
    }

    public synchronized void changeFriend(String friendUsername) {

        DebugLog.log(TAG, "about to set hasFriend=true for new friend username '" + friendUsername + "'");

        friend = new UserPosition();
        friend.setUsername(friendUsername);
        hasFriend = true;
        post.dispatchMessage(new Message(Message.Type.MODEL_UPDATE_SELECTED_FRIEND));
    }

    public String serializeToJson() {
        // pack up the settings
        JsonObject settingsObj = new JsonObject();
        settingsObj.addProperty(JsonKeys.HAS_USER, hasUser);
        settingsObj.addProperty(JsonKeys.HAS_FRIEND, hasFriend);
        settingsObj.addProperty(JsonKeys.IS_LISTENING, isListening);
        settingsObj.addProperty(JsonKeys.IS_TRANSMITTING, isTransmitting);

        JsonObject serial = new JsonObject();
        serial.add(JsonKeys.SETTINGS_DATA, settingsObj);

        // pack up user and friend, if they exist
        if(hasUser()) {
            serial.add(JsonKeys.USER_DATA, user.toJsonObj());
        }
        if(hasFriend()) {
            serial.add(JsonKeys.FRIEND_DATA, friend.toJsonObj());
        }

        // add the friend list (empty string OK)
        serial.addProperty(JsonKeys.FRIEND_LIST, friendList);
        return serial.toString();
    }

    public void loadFromJson(String jsonStr) {
        // grab the settings first
        JsonObject serial = JsonUtils.getJsonObject(jsonStr);
        JsonObject settingsObj = serial.getAsJsonObject(JsonKeys.SETTINGS_DATA);
        DebugLog.log(TAG, "loading settings from stored json: " + settingsObj);
        hasUser = settingsObj.get(JsonKeys.HAS_USER).getAsBoolean();
        hasFriend = settingsObj.get(JsonKeys.HAS_FRIEND).getAsBoolean();
        isListening = settingsObj.get(JsonKeys.IS_LISTENING).getAsBoolean();
        isTransmitting = settingsObj.get(JsonKeys.IS_TRANSMITTING).getAsBoolean();
        setFriendList(serial.get(JsonKeys.FRIEND_LIST).getAsString());

        // if app had a user, recreate
        if(hasUser()) {
            JsonObject userObj = serial.getAsJsonObject(JsonKeys.USER_DATA);
            DebugLog.log(TAG, "loading user from stored json: " + userObj);
            if(userObj != null) {
                user = new UserPosition();
                user.setUsername(userObj.get(JsonKeys.USERNAME).getAsString());
                user.setLatitude(userObj.get(JsonKeys.LATITUDE).getAsDouble());
                user.setLongitude(userObj.get(JsonKeys.LONGITUDE).getAsDouble());
                user.setAccuracy(userObj.get(JsonKeys.ACCURACY).getAsDouble());
                user.setEpochTime(userObj.get(JsonKeys.EPOCH_TIME).getAsLong());
            }
        }

        // and if app had a friend, recreate
        if(hasFriend()) {
            JsonObject friendObj = serial.getAsJsonObject(JsonKeys.FRIEND_DATA);
            DebugLog.log(TAG, "loading friend from stored json: " + friendObj);
            if(friendObj != null) {
                friend = new UserPosition();
                friend.setUsername(friendObj.get(JsonKeys.USERNAME).getAsString());
                friend.setLatitude(friendObj.get(JsonKeys.LATITUDE).getAsDouble());
                friend.setLongitude(friendObj.get(JsonKeys.LONGITUDE).getAsDouble());
                friend.setAccuracy(friendObj.get(JsonKeys.ACCURACY).getAsDouble());
                friend.setEpochTime(friendObj.get(JsonKeys.EPOCH_TIME).getAsLong());
            }
        }

        // send out update messages to get controls set up
        Message.Type type;
        type = (isListening() ? Message.Type.CONTROL_TOGGLE_LISTEN_ON : Message.Type.CONTROL_TOGGLE_LISTEN_OFF);
        post.dispatchMessage(new Message(type));
        type = (isTransmitting() ? Message.Type.CONTROL_TOGGLE_TRANSMIT_ON : Message.Type.CONTROL_TOGGLE_TRANSMIT_OFF);
        post.dispatchMessage(new Message(type));
    }
}
