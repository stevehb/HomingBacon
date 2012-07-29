package net.cruciblesoftware.homingbacon.client;

import java.net.URLEncoder;

class UrlBuilder {
    private static final String TAG = "HB: " + UrlBuilder.class.getSimpleName();
    private StringBuilder urlBuff;
    private boolean hasParam = false;

    UrlBuilder() {
        urlBuff = new StringBuilder();
    }

    UrlBuilder(String baseUrl) {
        urlBuff = new StringBuilder(baseUrl);
    }

    UrlBuilder setHost(String host) {
        urlBuff = new StringBuilder(host);
        return this;
    }

    UrlBuilder addParam(String key, String value) {
        if(!hasParam) {
            urlBuff.append("?");
            hasParam = true;
        } else {
            urlBuff.append("&");
        }
        urlBuff.append(URLEncoder.encode(key)).append("=").append(URLEncoder.encode(value));
        return this;
    }

    @Override
    public String toString() {
        return urlBuff.toString();
    }

}
