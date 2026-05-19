package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.model.GenEnumVariant;
import org.jspecify.annotations.Nullable;

public record StalwartEnumVariant(
        String name,
        String label,
        @Nullable String explanation,
        @Nullable String color) {
    public GenEnumVariant toModel(Context ctx) {
        return new GenEnumVariant(name, ctx.jmapToEnumConstant(name), label, explanation);
    }
}
