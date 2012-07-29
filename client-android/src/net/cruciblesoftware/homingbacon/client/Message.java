package net.cruciblesoftware.homingbacon.client;

class Message {
    enum Type {
        ON_PAUSE,
        ON_RESUME,
        NEW_USER_LOCATION,
        NEW_USERNAME,
        NEW_FRIEND_LOCATION,
        UPDATE_CONTROL_STATE,
        SERVER_RESPONSE
    }

    interface Listener {
        void receiveMessage(Message message);
    }

    Type type;
    String data;

    Message(Type type, String data) {
        this.type = type;
        this.data = data;
    }
}
