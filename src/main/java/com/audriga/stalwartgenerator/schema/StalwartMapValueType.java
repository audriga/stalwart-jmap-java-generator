package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.jmap.gson.RenameTag;
import com.google.common.base.CaseFormat;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartMapValueType {
    TypeName toJavaType(Context ctx);

    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength) implements StalwartMapValueType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ClassName.get(java.lang.String.class);
        }
    }

    record Number(
            StalwartNumberFormat format,
            @Nullable Integer min,
            @Nullable Integer max) implements StalwartMapValueType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return switch (format) {
                case integer, unsignedInteger -> TypeName.INT;
                case float_ -> TypeName.DOUBLE;
                case size -> TypeName.LONG;
                case duration -> TypeName.get(Duration.class);
            };
        }
    }

    record Enum(java.lang.String enumName) implements StalwartMapValueType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ctx.enumType(enumName);
        }
    }

    record Object(java.lang.String objectName) implements StalwartMapValueType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ctx.type(objectName);
        }
    }
}
