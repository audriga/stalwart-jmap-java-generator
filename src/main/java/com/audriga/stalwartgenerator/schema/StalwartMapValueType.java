package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import org.jspecify.annotations.Nullable;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartMapValueType {
    record String(
            StalwartStringFormat format,
            @Nullable Integer minLength,
            @Nullable Integer maxLength) implements StalwartMapValueType {
    }

    record Number(
            StalwartNumberFormat format,
            @Nullable Integer min,
            @Nullable Integer max) implements StalwartMapValueType {
    }

    record Enum(java.lang.String enumName) implements StalwartMapValueType  {
    }

    record Object(java.lang.String objectName) implements StalwartMapValueType {
    }
}
