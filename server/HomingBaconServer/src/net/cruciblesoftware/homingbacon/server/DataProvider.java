package net.cruciblesoftware.homingbacon.server;

import net.cruciblesoftware.homingbacon.DatastoreNames;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

class DataProvider {

    DatastoreService datastore;

    DataProvider() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    boolean hasUser(String username) {
        // check whether user exists, throw if they do
        Key userKey = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
        try {
            datastore.get(userKey);
            return true;
        } catch(EntityNotFoundException e) {
            return false;
        }
    }

    void addUser(String username) {
        // check whether user exists, throw if they do
        Key userKey = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
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
        Query q = new Query(DatastoreNames.KIND_USER_ROOT);
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
        Key userRoot = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get friend list child entity
        Query q = new Query(DatastoreNames.KIND_FRIEND_LIST, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity friendsEntity = pq.asSingleEntity();
        if(friendsEntity == null) {
            friendsEntity = addDefaultFriendListEntity(user);
        }

        // retrieve current list and add new name
        String friends = (String)friendsEntity.getProperty(DatastoreNames.PROP_FRIEND_LIST);
        if(friends.isEmpty()) {
            friends = friend;
        } else {
            String[] friendArray = friends.split(",");
            // ugh this feels bad - there has to be a better way
            boolean hasThisFriend = false;
            for(String s : friendArray) {
                if(s.equalsIgnoreCase(friend)) {
                    hasThisFriend = true;
                }
            }
            if(!hasThisFriend) {
                friends += "," + friend;
            }
        }
        friendsEntity.setProperty(DatastoreNames.PROP_FRIEND_LIST, friends);
        datastore.put(friendsEntity);
    }

    String getFriendList(String username) {
        // get user
        Key userRoot = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get friend list child entity
        Query q = new Query(DatastoreNames.KIND_FRIEND_LIST, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity friendsEntity = pq.asSingleEntity();
        if(friendsEntity == null || !friendsEntity.hasProperty(DatastoreNames.PROP_FRIEND_LIST)) {
            friendsEntity = addDefaultFriendListEntity(user);
        }
        return (String)friendsEntity.getProperty(DatastoreNames.PROP_FRIEND_LIST);
    }

    void setLastKnownPosition(String username, LastKnownPosition pos) {
        // get user
        Key userRoot = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get last known position child entity
        Query q = new Query(DatastoreNames.KIND_LAST_KNOWN_POSITION, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null) {
            lkpEntity = addDefaultLastKnownPositionEntity(user);
        }

        lkpEntity.setProperty(DatastoreNames.PROP_LATITUDE, pos.latitude);
        lkpEntity.setProperty(DatastoreNames.PROP_LONGITUDE, pos.longitude);
        lkpEntity.setProperty(DatastoreNames.PROP_ACCURACY, pos.accuracy);
        lkpEntity.setProperty(DatastoreNames.PROP_EPOCH_TIME, pos.epochTime);
        datastore.put(lkpEntity);
    }

    LastKnownPosition getLastKnownPosition(String username) {
        // get user
        Key userRoot = KeyFactory.createKey(DatastoreNames.KIND_USER_ROOT, username);
        Entity user = getUserEntity(userRoot);

        // get last known position child entity
        Query q = new Query(DatastoreNames.KIND_LAST_KNOWN_POSITION, userRoot);
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_LATITUDE) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_LONGITUDE) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_ACCURACY) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_EPOCH_TIME)) {
            lkpEntity = addDefaultLastKnownPositionEntity(user);
        }

        LastKnownPosition pos = new LastKnownPosition();
        pos.latitude = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_LATITUDE).toString());
        pos.longitude = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_LONGITUDE).toString());
        pos.accuracy = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_ACCURACY).toString());
        pos.epochTime = Long.parseLong(lkpEntity.getProperty(DatastoreNames.PROP_EPOCH_TIME).toString());
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
        Entity positionEntity = new Entity(DatastoreNames.KIND_LAST_KNOWN_POSITION, user.getKey());
        positionEntity.setProperty(DatastoreNames.PROP_LATITUDE, new Float(0.0));
        positionEntity.setProperty(DatastoreNames.PROP_LONGITUDE, new Float(0.0));
        positionEntity.setProperty(DatastoreNames.PROP_ACCURACY, new Float(0.0));
        positionEntity.setProperty(DatastoreNames.PROP_EPOCH_TIME, new Long(0L));
        datastore.put(positionEntity);
        return positionEntity;
    }

    private Entity addDefaultFriendListEntity(Entity user) {
        Entity friendListEntity = new Entity(DatastoreNames.KIND_FRIEND_LIST, user.getKey());
        friendListEntity.setProperty(DatastoreNames.PROP_FRIEND_LIST, "");
        datastore.put(friendListEntity);
        return friendListEntity;
    }
}
