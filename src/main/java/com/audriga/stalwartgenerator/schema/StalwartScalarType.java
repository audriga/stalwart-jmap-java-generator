package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartScalarType {
    TypeName toJavaType(Context ctx);

    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength) implements StalwartScalarType {
        @Override
        public TypeName toJavaType(Context ctx) {
            // TODO: handle different formats
            return ClassName.get(java.lang.String.class);
        }
    }

    record ObjectId(java.lang.String objectName) implements StalwartScalarType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ClassName.get(java.lang.String.class);
        }
    }

    record Enum(java.lang.String enumName) implements StalwartScalarType  {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ctx.type(enumName);
        }
    }
}
