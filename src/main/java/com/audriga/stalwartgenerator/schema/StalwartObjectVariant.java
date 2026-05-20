package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.model.GenSealed;
import org.jspecify.annotations.Nullable;

public record StalwartObjectVariant(String name, String label, @Nullable String schemaName) {
    public GenSealed.Variant toModel(Context ctx) {
        return new GenSealed.Variant(name, ctx.escapeName(name), label, schemaName != null ? ctx.type(schemaName) : null);
    }
}
