package com.netflix.conductor.sdk.workflow.utils;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder {
    private Map<String, Object> map = new HashMap<>();

    public MapBuilder add(String key, String value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder add(String key, Number value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder add(String key, MapBuilder value) {
        map.put(key, value.build());
        return this;
    }

    public Map<String, Object> build() {
        return map;
    }
}
