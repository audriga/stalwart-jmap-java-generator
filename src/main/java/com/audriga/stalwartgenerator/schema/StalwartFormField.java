package com.audriga.stalwartgenerator.schema;

import org.jspecify.annotations.Nullable;

public record StalwartFormField(
        String name,
        String label,
        @Nullable String keyLabel,
        @Nullable String valueLabel,
        @Nullable String placeholder) {
}
