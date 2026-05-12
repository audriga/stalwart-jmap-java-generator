package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import java.util.List;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartObjectSchema {
    record Single(String schemaName) implements StalwartObjectSchema {
    }

    record Multiple(List<StalwartObjectVariant> variants) implements StalwartObjectSchema {
    }
}
