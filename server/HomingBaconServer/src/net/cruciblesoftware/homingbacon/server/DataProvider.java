package net.cruciblesoftware.homingbacon.server;

import java.util.Date;

import net.cruciblesoftware.homingbacon.DatastoreNames;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

class DataProvider {

    private DatastoreService datastore;

    DataProvider() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    boolean hasUser(String username) {
        // query for UserData entity that matches username
        Query q = new Query(DatastoreNames.KIND_USER_DATA);
        q.setFilter(new Query.FilterPredicate(DatastoreNames.PROP_USERNAME, Query.FilterOperator.EQUAL, username));
        PreparedQuery pq = datastore.prepare(q);

        // return results
        boolean foundUser = false;
        for(Entity e : pq.asIterable()) {
            foundUser = true;
            break;
        }
        return foundUser;
    }

    void addUser(String username) {
        if(hasUser(username)) {
            throw new IllegalArgumentException("already have user '" + username + "'");
        }

        // create root entity and put to complete key
        Entity userRoot = new Entity(DatastoreNames.KIND_USER_ROOT);
        datastore.put(userRoot);
        addDefaultUserDataEntity(userRoot, username);
        addDefaultFriendListEntity(userRoot);
        addDefaultLastKnownPositionEntity(userRoot);
    }


    String getUserList(String paramUsername) {
        Query q = new Query(DatastoreNames.KIND_USER_DATA);
        PreparedQuery pq = datastore.prepare(q);

        StringBuilder buff = new StringBuilder();
        String tmpStr = null;
        for(Entity e : pq.asIterable()) {
            if(buff.length() == 0) {
                tmpStr = (String)e.getProperty(DatastoreNames.PROP_USERNAME);
                buff.append(tmpStr == null ? "" : tmpStr);
            } else {
                tmpStr = (String)e.getProperty(DatastoreNames.PROP_USERNAME);
                buff.append(",");
                buff.append(tmpStr == null ? "" : tmpStr);
            }
        }
        return (buff.length() == 0 ? "" : buff.toString());
    }

    void addFriend(String username, String friend) {
        // get friend list entity for the user
        Entity userRoot = getUserRoot(username);
        if(userRoot == null) {
            throw new IllegalArgumentException("no such user '" + username + "'");
        }

        Query q = new Query(DatastoreNames.KIND_FRIEND_LIST, userRoot.getKey());
        PreparedQuery pq = datastore.prepare(q);
        Entity friendListEntity = pq.asSingleEntity();
        if(friendListEntity == null) {
            friendListEntity = addDefaultFriendListEntity(userRoot);
        }

        // ensure name is new, add to list of those we are following
        String list = (String)friendListEntity.getProperty(DatastoreNames.PROP_FOLLOWING);
        if(list.isEmpty()) {
            list = friend;
        } else {
            String[] friendArray = list.split(",");
            // ugh this feels bad - there has to be a better way
            boolean isFollowing = false;
            for(String s : friendArray) {
                if(s.equalsIgnoreCase(friend)) {
                    isFollowing = true;
                    break;
                }
            }
            if(!isFollowing) {
                list += "," + friend;
            }
        }
        friendListEntity.setProperty(DatastoreNames.PROP_FOLLOWING, list);
        datastore.put(friendListEntity);

        // get friend list entity for the friend
        userRoot = getUserRoot(friend);
        q = new Query(DatastoreNames.KIND_FRIEND_LIST, userRoot.getKey());
        pq = datastore.prepare(q);
        friendListEntity = pq.asSingleEntity();
        if(friendListEntity == null) {
            friendListEntity = addDefaultFriendListEntity(userRoot);
        }

        // ensure name is new, add to list of followers
        list = (String)friendListEntity.getProperty(DatastoreNames.PROP_FOLLOWED_BY);
        if(list.isEmpty()) {
            list = username;
        } else {
            String[] friendArray = list.split(",");
            boolean isFollowedBy = false;
            for(String s : friendArray) {
                if(s.equalsIgnoreCase(username)) {
                    isFollowedBy = true;
                    break;
                }
            }
            if(!isFollowedBy) {
                list += "," + username;
            }
        }
        friendListEntity.setProperty(DatastoreNames.PROP_FOLLOWED_BY, list);
        datastore.put(friendListEntity);
    }

    String getFriendList(String username) {
        // get user
        Entity userRoot = getUserRoot(username);
        if(userRoot == null) {
            throw new IllegalArgumentException("no such user '" + username + "'");
        }

        // get friend list child entity
        Query q = new Query(DatastoreNames.KIND_FRIEND_LIST, userRoot.getKey());
        PreparedQuery pq = datastore.prepare(q);
        Entity friendsEntity = pq.asSingleEntity();
        if(friendsEntity == null || !friendsEntity.hasProperty(DatastoreNames.PROP_FOLLOWING)) {
            friendsEntity = addDefaultFriendListEntity(userRoot);
        }
        String list = (String)friendsEntity.getProperty(DatastoreNames.PROP_FOLLOWING);
        return (list == null || list.isEmpty() ? "" : list);
    }

    void setLastKnownPosition(String username, SimplePosition pos) {
        // get user
        Entity userRoot = getUserRoot(username);
        if(userRoot == null) {
            throw new IllegalArgumentException("no such user '" + username + "'");
        }

        // get last known position child entity
        Query q = new Query(DatastoreNames.KIND_LAST_KNOWN_POSITION, userRoot.getKey());
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null) {
            lkpEntity = addDefaultLastKnownPositionEntity(userRoot);
        }

        lkpEntity.setProperty(DatastoreNames.PROP_LATITUDE, pos.simpleLatitude);
        lkpEntity.setProperty(DatastoreNames.PROP_LONGITUDE, pos.simpleLongitude);
        lkpEntity.setProperty(DatastoreNames.PROP_ACCURACY, pos.simpleAccuracy);
        lkpEntity.setProperty(DatastoreNames.PROP_EPOCH_TIME, pos.simpleEpochTime);
        datastore.put(lkpEntity);
    }

    SimplePosition getLastKnownPosition(String username) {
        // get user
        Entity userRoot = getUserRoot(username);
        if(userRoot == null) {
            throw new IllegalArgumentException("no such user '" + username + "'");
        }

        // get last known position child entity
        Query q = new Query(DatastoreNames.KIND_LAST_KNOWN_POSITION, userRoot.getKey());
        PreparedQuery pq = datastore.prepare(q);
        Entity lkpEntity = pq.asSingleEntity();
        if(lkpEntity == null ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_LATITUDE) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_LONGITUDE) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_ACCURACY) ||
                !lkpEntity.hasProperty(DatastoreNames.PROP_EPOCH_TIME)) {
            lkpEntity = addDefaultLastKnownPositionEntity(userRoot);
        }

        SimplePosition pos = new SimplePosition();
        pos.simpleLatitude = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_LATITUDE).toString());
        pos.simpleLongitude = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_LONGITUDE).toString());
        pos.simpleAccuracy = Double.parseDouble(lkpEntity.getProperty(DatastoreNames.PROP_ACCURACY).toString());
        pos.simpleEpochTime = Long.parseLong(lkpEntity.getProperty(DatastoreNames.PROP_EPOCH_TIME).toString());
        return pos;
    }

    private Entity getUserRoot(String username) {
        if(!hasUser(username)) {
            return null;
        }

        // get root key for this username
        Query q = new Query(DatastoreNames.KIND_USER_DATA);
        q.setFilter(new Query.FilterPredicate(DatastoreNames.PROP_USERNAME, Query.FilterOperator.EQUAL, username));
        PreparedQuery pq = datastore.prepare(q);
        Entity userData = pq.asSingleEntity();
        Key userRootKey = userData.getParent();

        // get user root entity
        Entity userRoot = null;
        try {
            userRoot = datastore.get(userRootKey);
        } catch(EntityNotFoundException e) {
            throw new IllegalArgumentException("user root for '" + username + "' not found");
        }
        return userRoot;
    }

    private Entity addDefaultUserDataEntity(Entity userRoot, String username) {
        Entity userData = new Entity(DatastoreNames.KIND_USER_DATA, userRoot.getKey());
        userData.setProperty(DatastoreNames.PROP_USERNAME, username);
        userData.setProperty(DatastoreNames.PROP_CREATED, new Date());
        datastore.put(userData);
        return userData;
    }

    private Entity addDefaultFriendListEntity(Entity userRoot) {
        Entity friendList = new Entity(DatastoreNames.KIND_FRIEND_LIST, userRoot.getKey());
        friendList.setProperty(DatastoreNames.PROP_FOLLOWING, "");
        friendList.setProperty(DatastoreNames.PROP_FOLLOWED_BY, "");
        datastore.put(friendList);
        return friendList;
    }

    private Entity addDefaultLastKnownPositionEntity(Entity userRoot) {
        Entity lastKnownPosition = new Entity(DatastoreNames.KIND_LAST_KNOWN_POSITION, userRoot.getKey());
        lastKnownPosition.setProperty(DatastoreNames.PROP_LATITUDE, new Float(0.0));
        lastKnownPosition.setProperty(DatastoreNames.PROP_LONGITUDE, new Float(0.0));
        lastKnownPosition.setProperty(DatastoreNames.PROP_ACCURACY, new Float(0.0));
        lastKnownPosition.setProperty(DatastoreNames.PROP_EPOCH_TIME, new Long(0L));
        datastore.put(lastKnownPosition);
        return lastKnownPosition;
    }
}
