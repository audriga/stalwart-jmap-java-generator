package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartFieldType {
    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength,
            boolean nullable) implements StalwartFieldType {
    }

    record Number(
            StalwartNumberFormat format,
            @Nullable Integer min,
            @Nullable Integer max,
            boolean nullable) implements StalwartFieldType {
    }

    record UtcDateTime(boolean nullable) implements StalwartFieldType {
    }

    record Boolean() implements StalwartFieldType {
    }

    record Enum(java.lang.String enumName, boolean nullable) implements StalwartFieldType {
    }

    record BlobId() implements StalwartFieldType {
    }

    record ObjectId(java.lang.String objectName, boolean nullable) implements StalwartFieldType {
    }

    record Object(java.lang.String objectName, boolean nullable) implements StalwartFieldType {
    }

    record ObjectList(
            java.lang.String objectName,
            @Nullable Integer minItems,
            @Nullable Integer maxItems) implements StalwartFieldType {
    }

    record Set(
            @SerializedName("class") StalwartScalarType clazz,
            @Nullable Integer minItems,
            @Nullable Integer maxItems) implements StalwartFieldType {
    }

    record Map(
            StalwartScalarType keyClass,
            StalwartMapValueType valueClass,
            @Nullable Integer minItems,
            @Nullable Integer maxItems) implements StalwartFieldType {
    }
}
