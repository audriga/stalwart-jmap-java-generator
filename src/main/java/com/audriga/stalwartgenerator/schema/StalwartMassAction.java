package com.audriga.stalwartgenerator.schema;

import com.audriga.jmap.gson.RenameTag;
import com.google.common.base.CaseFormat;
import java.util.Map;
import com.google.gson.JsonElement;

@RenameTag(CaseFormat.LOWER_CAMEL)
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
