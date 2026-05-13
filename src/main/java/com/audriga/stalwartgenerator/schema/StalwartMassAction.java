package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import java.util.Map;
import com.google.gson.JsonElement;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartMassAction {
    record SetProperty(
            String label,
            Map<String, JsonElement> properties) implements StalwartMassAction {
    }

    record Delete(String label) implements StalwartMassAction {
    }

    record Separator() implements StalwartMassAction {
    }
}
