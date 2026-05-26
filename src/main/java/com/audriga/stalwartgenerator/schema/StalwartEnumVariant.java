package com.audriga.stalwartgenerator.schema;

import org.jspecify.annotations.Nullable;

public record StalwartEnumVariant(
        String name,
        String label,
        @Nullable String explanation,
        @Nullable String color) {}
