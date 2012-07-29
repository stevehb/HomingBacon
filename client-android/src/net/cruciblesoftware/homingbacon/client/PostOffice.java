package net.cruciblesoftware.homingbacon.client;

import java.util.ArrayList;

class PostOffice {
    private static final String TAG = "HB: " + PostOffice.class.getSimpleName();

    private ArrayList<Message.Listener> listeners;

    PostOffice() {
        DebugLog.log(TAG, "creating the PostOffice");
        listeners = new ArrayList<Message.Listener>();
    }

    void registerListener(Message.Listener listener) {
        listeners.add(listener);
    }

    void unregisterListener(Message.Listener listener) {
        if(!listeners.remove(listener)) {
            DebugLog.log(TAG, "WARNING: tried to remove listener that wasn't added");
        }
    }

    void dispatchMessage(Message message) {
        for(Message.Listener l : listeners) {
            l.receiveMessage(message);
        }
    }
}
