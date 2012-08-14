package net.cruciblesoftware.homingbacon;

public class DatastoreNames {
    public static final String KIND_USER_ROOT = "UserRoot";

    public static final String KIND_USER_DATA = "UserData";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_CREATED = "created";

    public static final String KIND_FRIEND_LIST = "FriendList";
    public static final String PROP_FOLLOWING = "following";
    public static final String PROP_FOLLOWED_BY = "followedBy";

    public static final String KIND_LAST_KNOWN_POSITION = "LastKnownPosition";
    public static final String PROP_LATITUDE = "latitude";
    public static final String PROP_LONGITUDE = "longitude";
    public static final String PROP_ACCURACY = "accuracy";
    public static final String PROP_EPOCH_TIME = "epochTime";
}
