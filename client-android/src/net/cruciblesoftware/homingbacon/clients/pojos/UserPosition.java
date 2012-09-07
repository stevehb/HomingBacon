package net.cruciblesoftware.homingbacon.clients.pojos;

import net.cruciblesoftware.homingbacon.JsonKeys;
import net.cruciblesoftware.homingbacon.JsonUtils;
import net.cruciblesoftware.homingbacon.Position;

import com.google.gson.JsonObject;

public class UserPosition extends Position {
    protected String username;
    protected boolean hasData;

    public boolean hasData() {
        return hasData;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setLatitude(double latitude) {
        super.setLatitude(latitude);
        hasData = true;
    }

    @Override
    public void setLongitude(double longitude) {
        super.setLongitude(longitude);
        hasData = true;
    }

    @Override
    public void setAccuracy(double accuracy) {
        super.setAccuracy(accuracy);
        hasData = true;
    }

    @Override
    public void setEpochTime(long epochTime) {
        super.setEpochTime(epochTime);
        hasData = true;
    }

    public String toJsonStr() {
        JsonObject jsonObj = toJsonObj();
        return jsonObj.toString();
    }

    public JsonObject toJsonObj() {
        JsonObject jsonObj = JsonUtils.packPosition(this);
        jsonObj.addProperty(JsonKeys.USERNAME, username);
        return jsonObj;
    }

    public UserPosition fromJson(String json) {
        JsonObject jsonObj = JsonUtils.getJsonObject(json);
        username = jsonObj.get(JsonKeys.USERNAME).getAsString();
        Position pos = JsonUtils.parsePosition(jsonObj);
        latitude = pos.getLatitude();
        longitude = pos.getLongitude();
        accuracy = pos.getAccuracy();
        epochTime = pos.getEpochTime();
        return this;
    }

    public void clone(UserPosition userPosition) {
        this.username = userPosition.username;
        this.latitude = userPosition.latitude;
        this.longitude = userPosition.longitude;
        this.accuracy = userPosition.accuracy;
        this.epochTime = userPosition.epochTime;
    }
}
