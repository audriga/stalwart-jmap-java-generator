package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import java.util.Map;
import com.google.gson.JsonElement;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartItemAction {
    record Delete(String label) implements StalwartItemAction {
    }

    record SetProperty(
            String label,
            Map<String, JsonElement> properties) implements StalwartItemAction {
    }

    record Query(
            String label,
            String objectName,
            String fieldName) implements StalwartItemAction {
    }

    record View(String label, String objectName) implements StalwartItemAction {
    }

    record Separator() implements StalwartItemAction {
    }
}
