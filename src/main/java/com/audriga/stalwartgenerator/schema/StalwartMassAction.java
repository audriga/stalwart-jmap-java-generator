package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.RenameTag;
import com.google.common.base.CaseFormat;
import com.google.gson.JsonElement;
import java.util.Map;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartMassAction {
    record SetProperty(String label, Map<String, JsonElement> properties) implements StalwartMassAction {}

    record Delete(String label) implements StalwartMassAction {}

    record Separator() implements StalwartMassAction {}
}
