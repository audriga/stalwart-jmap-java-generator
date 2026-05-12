package com.audriga.stalwartgenerator.schema;

import org.jspecify.annotations.Nullable;

public record StalwartObjectVariant(String name, String label, @Nullable String schemaName) {
}
