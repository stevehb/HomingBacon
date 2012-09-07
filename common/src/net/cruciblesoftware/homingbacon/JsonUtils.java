package net.cruciblesoftware.homingbacon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtils {
    public static JsonObject getJsonObject(String jsonStr) {
        try {
            return new JsonParser().parse(jsonStr).getAsJsonObject();
        } catch(IllegalStateException e) {
            throw new IllegalStateException("ERROR: failed to parse '" + jsonStr + "' as JSON: " + e.getLocalizedMessage());
        }
    }

    public static JsonObject createJson(Object... params) {
        JsonObject jsonObj = new JsonObject();
        for(int i = 0; i < params.length; i += 2) {
            String key = (String)params[i];
            Object val = params[i+1];
            if(val instanceof Boolean) {
                jsonObj.addProperty(key, (Boolean)val);
            } else if(val instanceof Character) {
                jsonObj.addProperty(key, (Character)val);
            } else if(val instanceof Number) {
                jsonObj.addProperty(key, (Number)val);
            } else if(val instanceof String) {
                jsonObj.addProperty(key, (String)val);
            } else {
                jsonObj.addProperty(key, val.toString());
            }
        }
        return jsonObj;
    }

    public static Position parsePosition(JsonObject jsonObj) {
        Position pos = new Position();
        pos.latitude = jsonObj.get(JsonKeys.LATITUDE).getAsDouble();
        pos.longitude = jsonObj.get(JsonKeys.LONGITUDE).getAsDouble();
        pos.accuracy = jsonObj.get(JsonKeys.ACCURACY).getAsDouble();
        pos.epochTime = jsonObj.get(JsonKeys.EPOCH_TIME).getAsLong();
        return pos;
    }

    public static JsonObject packPosition(Position pos) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty(JsonKeys.LATITUDE, pos.latitude);
        jsonObj.addProperty(JsonKeys.LONGITUDE, pos.longitude);
        jsonObj.addProperty(JsonKeys.ACCURACY, pos.accuracy);
        jsonObj.addProperty(JsonKeys.EPOCH_TIME, pos.epochTime);
        return jsonObj;
    }

    public static String getServerErrorMessage(String jsonStr) {
        JsonObject jsonObj = getJsonObject(jsonStr);
        String status = jsonObj.get(JsonKeys.STATUS).getAsString();
        if(status.equals(JsonValues.ERROR)) {
            return jsonObj.get(JsonKeys.MESSAGE).getAsString();
        } else {
            return null;
        }
    }
}
