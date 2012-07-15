package net.cruciblesoftware.homingbacon.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

class DataProvider {
    private static final String KIND_USER_ROOT = "UserRoot";

    private static final String KIND_FRIEND_LIST = "FriendList";
    private static final String PROP_NAME_FRIEND_LIST = "friends";

    private static final String KIND_LAST_KNOWN_POSITION = "LastKnownPosition";
    private static final String PROP_NAME_LATITUDE = "latitude";
    private static final String PROP_NAME_LONGITUDE = "longitude";
    private static final String PROP_NAME_ACCURACY = "accuracy";
    private static final String PROP_NAME_EPOCH_TIME = "epochTime";

    DatastoreService datastore;

    DataProvider() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    void addUser(String username) {
        // check whether user exists, throw if they do
        Key userKey = KeyFactory.createKey(KIND_USER_ROOT, username);
        try {
            datastore.get(userKey);
            throw new IllegalArgumentException("already have user '" + username + "'");
        } catch(EntityNotFoundException e) { }

        Entity user = new Entity(userKey);
        datastore.put(user);
        addDefaultFriendListEntity(user);
        addDefaultLastKnownPositionEntity(user);
    }

    String getUserList(String paramUsername) {
        Query q = new Query(KIND_USER_ROOT);
        PreparedQuery pq = datastore.prepare(q);

        StringBuilder buff = new StringBuilder();
        for(Entity e : pq.asIterable()) {
            if(buff.length() == 0) {
                buff.append(e.getKey().getName());
            } else {
                buff.append(",");
                buff.append(e.getKey().getName());
            }
        }
        return buff.toString();
    }

    void addFriend(String username, String friend) {
        // get user
        Key userRoot = KeyFactory.createKey(KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get friend list child entity
        Query q = new Query(KIND_FRIEND_LIST, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity friendsEntity = pq.asSingleEntity();
        if(friendsEntity == null) {
            friendsEntity = addDefaultFriendListEntity(user);
        }

        // retrieve current list and add new name
        String friends = (String)friendsEntity.getProperty(PROP_NAME_FRIEND_LIST);
        if(friends.isEmpty()) {
            friends = friend;
        } else {
            friends += "," + friend;
        }
        friendsEntity.setProperty(PROP_NAME_FRIEND_LIST, friends);
        datastore.put(friendsEntity);
    }

    String getFriendList(String username) {
        // get user
        Key userRoot = KeyFactory.createKey(KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get friend list child entity
        Query q = new Query(KIND_FRIEND_LIST, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity friendsEntity = pq.asSingleEntity();
        if(friendsEntity == null || !friendsEntity.hasProperty(PROP_NAME_FRIEND_LIST)) {
            friendsEntity = addDefaultFriendListEntity(user);
        }
        return (String)friendsEntity.getProperty(PROP_NAME_FRIEND_LIST);
    }

    void setLastKnownPosition(String username, LastKnownPosition pos) {
        // get user
        Key userRoot = KeyFactory.createKey(KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get last known position child entity
        Query q = new Query(KIND_LAST_KNOWN_POSITION, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null) {
            lkpEntity = addDefaultLastKnownPositionEntity(user);
        }

        lkpEntity.setProperty(PROP_NAME_LATITUDE, pos.latitude);
        lkpEntity.setProperty(PROP_NAME_LONGITUDE, pos.longitude);
        lkpEntity.setProperty(PROP_NAME_ACCURACY, pos.accuracy);
        lkpEntity.setProperty(PROP_NAME_EPOCH_TIME, pos.epochTime);
        datastore.put(lkpEntity);
    }

    LastKnownPosition getLastKnownPosition(String username) {
        // get user
        Key userRoot = KeyFactory.createKey(KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get last known position child entity
        Query q = new Query(KIND_LAST_KNOWN_POSITION, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null ||
                !lkpEntity.hasProperty(PROP_NAME_LATITUDE) ||
                !lkpEntity.hasProperty(PROP_NAME_LONGITUDE) ||
                !lkpEntity.hasProperty(PROP_NAME_ACCURACY) ||
                !lkpEntity.hasProperty(PROP_NAME_EPOCH_TIME)) {
            lkpEntity = addDefaultLastKnownPositionEntity(user);
        }

        LastKnownPosition pos = new LastKnownPosition();
        pos.latitude = Double.parseDouble(lkpEntity.getProperty(PROP_NAME_LATITUDE).toString());
        pos.longitude = Double.parseDouble(lkpEntity.getProperty(PROP_NAME_LONGITUDE).toString());
        pos.accuracy = Double.parseDouble(lkpEntity.getProperty(PROP_NAME_ACCURACY).toString());
        pos.epochTime = Long.parseLong(lkpEntity.getProperty(PROP_NAME_EPOCH_TIME).toString());
        return pos;
    }

    private Entity getUserEntity(Key userRoot) {
        Entity user = null;
        try {
            user = datastore.get(userRoot);
        } catch(EntityNotFoundException e) {
            throw new IllegalArgumentException("user '" + userRoot.getName() + "' not found");
        }
        return user;
    }

    private Entity addDefaultLastKnownPositionEntity(Entity user) {
        Entity positionEntity = new Entity(KIND_LAST_KNOWN_POSITION, user.getKey());
        positionEntity.setProperty(PROP_NAME_LATITUDE, new Float(0.0));
        positionEntity.setProperty(PROP_NAME_LONGITUDE, new Float(0.0));
        positionEntity.setProperty(PROP_NAME_ACCURACY, new Float(0.0));
        positionEntity.setProperty(PROP_NAME_EPOCH_TIME, new Long(0L));
        datastore.put(positionEntity);
        return positionEntity;
    }

    private Entity addDefaultFriendListEntity(Entity user) {
        Entity friendListEntity = new Entity(KIND_FRIEND_LIST, user.getKey());
        friendListEntity.setProperty(PROP_NAME_FRIEND_LIST, "");
        datastore.put(friendListEntity);
        return friendListEntity;
    }
}
