package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import org.jspecify.annotations.Nullable;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartScalarType {
    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength) implements StalwartScalarType {
    }

    record ObjectId(java.lang.String objectName) implements StalwartScalarType {
    }

    record Enum(java.lang.String enumName) implements StalwartScalarType  {
    }
}
