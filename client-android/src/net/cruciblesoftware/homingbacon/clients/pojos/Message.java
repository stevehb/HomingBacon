package net.cruciblesoftware.homingbacon.clients.pojos;

public class Message {
    public enum Type {
        MODEL_CREATE_USER,
        MODEL_UPDATE_USER_USERNAME,
        MODEL_UPDATE_USER_POSITION,
        MODEL_UPDATE_FRIEND_POSITION,
        MODEL_UPDATE_SELECTED_FRIEND,
        MODEL_UPDATE_FRIEND_LIST,
        CONTROL_TOGGLE_TRANSMIT_ON,
        CONTROL_TOGGLE_TRANSMIT_OFF,
        CONTROL_TOGGLE_LISTEN_ON,
        CONTROL_TOGGLE_LISTEN_OFF,
        CONTROL_SPINNER_FRIEND_CHOSEN,
        SERVER_RESPONSE,
        GPS_RESPONSE,
        ON_PAUSE,
        ON_RESUME
    }

    public interface Listener {
        void receiveMessage(Message message);
    }

    public Type type;
    public String data;

    public Message(Type type) {
        this.type = type;
        this.data = null;
    }

    public Message(Type type, String data) {
        this.type = type;
        this.data = data;
    }
}
