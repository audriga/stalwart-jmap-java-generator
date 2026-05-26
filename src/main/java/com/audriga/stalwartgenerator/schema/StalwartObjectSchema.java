package com.audriga.stalwartgenerator.schema;

import com.audriga.jmap.gson.RenameTag;
import com.google.common.base.CaseFormat;
import java.util.List;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartObjectSchema {
    record Single(String schemaName) implements StalwartObjectSchema {
    }

    record Multiple(List<StalwartObjectVariant> variants) implements StalwartObjectSchema {
    }
}
