package com.matrix.comm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestMap {
    private Map<String, Object> requestMap = new HashMap();
    private final String accessToken;

    public RequestMap(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Set<String> keySet() {
        return this.requestMap.keySet();
    }

    public int size() {
        return this.requestMap.size();
    }

    public Object get(String key) {
        return this.requestMap.get(key) == null ? null : this.requestMap.get(key);
    }

    public String getString(String key) {
        String retStr = "";

        try {
            retStr = (String) this.requestMap.get(key);
            if (retStr == null || retStr.length() <= 0) {
                retStr = "";
            }
        } catch (Exception var4) {
            retStr = "";
        }

        return retStr;
    }

    public void setString(String key, String val) {
        this.requestMap.put(key, val);
    }

    public void put(String key, Object val) {
        this.requestMap.put(key, val);
    }
}
