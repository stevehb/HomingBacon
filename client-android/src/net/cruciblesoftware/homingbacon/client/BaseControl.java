package net.cruciblesoftware.homingbacon.client;

class BaseControl {
    private static final String TAG = "HB: " + BaseControl.class.getSimpleName();

    protected static PostOffice post;
    protected static ServerConnection server;
    protected static UserData userData;

    static {
        DebugLog.log(TAG, "creating post, server, and userData");
        post = new PostOffice();
        server = new ServerConnection(post);
        userData = new UserData(post);
    }
}
