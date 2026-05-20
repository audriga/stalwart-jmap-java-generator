package com.audriga.stalwartgenerator.schema;

import java.util.Map;
import java.util.stream.Stream;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.model.GenField;
import com.google.gson.JsonElement;

public record StalwartFields(Map<String, StalwartField> properties,
                             Map<String, JsonElement> defaults) {
    public StalwartFields {
        if (properties == null) properties = Map.of();
        if (defaults == null) defaults = Map.of();
    }

    public Stream<GenField> toModel(Context ctx) {
        return properties.entrySet().stream().map(entry -> {
            var name = entry.getKey();
            var javaName = ctx.escapeName(name);
            // TODO: make use of other field properties
            var type = entry.getValue().type();
            return new GenField(name, javaName, type.toJavaType(ctx), type.nullable(), defaults.get(name));
        });
    }
}
