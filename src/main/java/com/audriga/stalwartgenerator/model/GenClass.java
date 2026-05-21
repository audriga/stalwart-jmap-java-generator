package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.TypeSpec;

public interface GenClass {
    String schemaName();

    String javaName();

    // TypeSpec.toBuilder() is currently broken (https://github.com/palantir/javapoet/issues/341),
    // so we return the builder directly.
    TypeSpec.Builder generate(Context ctx);
}
