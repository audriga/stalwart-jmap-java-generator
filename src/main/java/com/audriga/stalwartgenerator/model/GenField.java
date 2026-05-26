package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.schema.StalwartFieldUpdate;
import com.google.gson.JsonElement;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public record GenField(
        String schemaName,
        String javaName,
        String description,
        StalwartFieldUpdate update,
        TypeName typeName,
        boolean nullable,
        @Nullable JsonElement defaultValue,
        boolean enterprise) {}
