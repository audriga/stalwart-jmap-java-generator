package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.RenameTag;
import com.google.common.base.CaseFormat;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartObjectType {
    sealed interface Real {
        String description();

        String permissionPrefix();

        boolean enterprise();
    }

    record Singleton(String description, String permissionPrefix, boolean enterprise)
            implements StalwartObjectType, Real {}

    record Object(String description, String permissionPrefix, boolean enterprise)
            implements StalwartObjectType, Real {}

    record View(String objectName) implements StalwartObjectType {}
}
