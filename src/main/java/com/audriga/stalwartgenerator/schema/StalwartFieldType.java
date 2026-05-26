package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.audriga.stalwartgenerator.gson.RenameTag;
import com.google.common.base.CaseFormat;
import com.google.gson.annotations.SerializedName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartFieldType {
    default boolean nullable() {
        return false;
    }

    TypeName toJavaType(Context ctx);

    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength,
            boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ClassName.get(java.lang.String.class);
        }
    }

    record Number(
            StalwartNumberFormat format,
            @Nullable Integer min,
            @Nullable Integer max,
            boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            var name =
                    switch (format) {
                        case integer, unsignedInteger -> TypeName.INT;
                        case float_ -> TypeName.DOUBLE;
                        case size -> TypeName.LONG;
                        case duration -> TypeName.get(Duration.class);
                    };
            return nullable ? name.box() : name;
        }
    }

    record UtcDateTime(boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ClassName.get(Instant.class);
        }
    }

    record Boolean() implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return TypeName.BOOLEAN;
        }
    }

    record Enum(java.lang.String enumName, boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ctx.enumType(enumName);
        }
    }

    record BlobId() implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            // jmap-java uses plain String
            return ClassName.get(java.lang.String.class);
        }
    }

    record ObjectId(java.lang.String objectName, boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            // TODO: introduce our own Id<T> type?
            return ClassName.get(java.lang.String.class);
        }
    }

    record Object(java.lang.String objectName, boolean nullable) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ctx.type(objectName);
        }
    }

    record ObjectList(
            java.lang.String objectName,
            @Nullable Integer minItems,
            @Nullable Integer maxItems) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return Types.map(TypeName.INT, ctx.type(objectName));
        }
    }

    record Set(
            @SerializedName("class") StalwartScalarType clazz,
            @Nullable Integer minItems,
            @Nullable Integer maxItems) implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return Types.map(clazz.toJavaType(ctx), TypeName.BOOLEAN);
        }
    }

    record Map(
            StalwartScalarType keyClass,
            StalwartMapValueType valueClass,
            @Nullable Integer minItems,
            @Nullable Integer maxItems)
            implements StalwartFieldType {
        @Override
        public TypeName toJavaType(Context ctx) {
            return ParameterizedTypeName.get(
                    ClassName.get(java.util.Map.class),
                    keyClass.toJavaType(ctx).box(),
                    valueClass.toJavaType(ctx).box());
        }
    }
}
