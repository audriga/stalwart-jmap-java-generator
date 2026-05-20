package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.TypeSpec;

public interface GenClass {
    String schemaName();

    String javaName();

    TypeSpec generate(Context ctx);
}
