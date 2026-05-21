package com.audriga.stalwartgenerator.schema;

import java.util.Map;

import com.google.gson.JsonElement;

public record StalwartFields(Map<String, StalwartField> properties,
                             Map<String, JsonElement> defaults) {
    public StalwartFields {
        if (properties == null) properties = Map.of();
        if (defaults == null) defaults = Map.of();
    }
}
