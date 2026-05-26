package com.audriga.stalwartgenerator.schema;

import com.google.gson.JsonElement;
import java.util.Map;

public record StalwartFields(Map<String, StalwartField> properties, Map<String, JsonElement> defaults) {
    public StalwartFields {
        if (properties == null) properties = Map.of();
        if (defaults == null) defaults = Map.of();
    }
}
