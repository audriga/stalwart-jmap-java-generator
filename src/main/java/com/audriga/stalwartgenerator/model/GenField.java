package com.audriga.stalwartgenerator.model;

import com.google.gson.JsonElement;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public record GenField(
        String name,
        String javaName,
        TypeName typeName,
        boolean nullable,
        @Nullable JsonElement defaultValue) {
}
