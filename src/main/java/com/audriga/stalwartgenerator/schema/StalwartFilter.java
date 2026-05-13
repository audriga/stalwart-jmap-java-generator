package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartFilter {
    String field();

    String label();

    record Text(String field, String label) implements StalwartFilter {
    }

    record Enum(String field, String enumLabel, String label) implements StalwartFilter {
    }

    record Integer(String field, String label) implements StalwartFilter {
    }

    record Date(String field, String label) implements StalwartFilter {
    }

    record ObjectId(String field, String objectName, String label) implements StalwartFilter {
    }
}
