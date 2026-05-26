package com.audriga.stalwartgenerator.schema;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public record StalwartList(
        String title,
        String subtitle,
        @Nullable String labelProperty,
        String singularName,
        String pluralName,
        List<StalwartColumn> columns,
        List<StalwartFilter> filters,
        Map<String, JsonElement> filtersStatic,
        List<String> sort,
        List<StalwartMassAction> massAction,
        List<StalwartItemAction> itemActions) {}
