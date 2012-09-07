package net.cruciblesoftware.homingbacon.clients.utils;

import java.util.ArrayList;

import net.cruciblesoftware.homingbacon.clients.pojos.Message;

/* TODO
 * Should messaging through the PostOffice be asynchronous? If so, we should
 * create a handler, then dispatchMesssage() should put its loop in a Runnable
 * and that Runnable should be post to the handler. This will ensure control
 * returns to the system before the message gets processed.
 */

public class PostOffice {
    private static final String TAG = "HB: " + PostOffice.class.getSimpleName();

    private ArrayList<Message.Listener> listeners;
    private static PostOffice instance;

    private PostOffice() {
        DebugLog.log(TAG, "creating the PostOffice");
        listeners = new ArrayList<Message.Listener>();
    }

    public static PostOffice getInstance() {
        if(instance == null) {
            instance = new PostOffice();
        }
        return instance;
    }

    public synchronized void registerListener(Message.Listener listener) {
        listeners.add(listener);
    }

    public synchronized void unregisterListener(Message.Listener listener) {
        if(!listeners.remove(listener)) {
            DebugLog.log(TAG, "WARNING: tried to remove listener that wasn't added");
        }
    }

    public synchronized void dispatchMessage(Message message) {
        DebugLog.log(TAG, "dispatching message type=" + message.type);
        for(Message.Listener l : listeners) {
            l.receiveMessage(message);
        }
    }
}
