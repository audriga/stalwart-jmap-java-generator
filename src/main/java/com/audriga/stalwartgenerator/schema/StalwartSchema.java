package com.audriga.stalwartgenerator.schema;

import java.util.List;
import java.util.Map;

public record StalwartSchema(
        Map<String, StalwartObjectType> objects,
        Map<String, StalwartObjectSchema> schemas,
        Map<String, StalwartFields> fields,
        Map<String, StalwartForm> forms,
        Map<String, StalwartList> lists,
        Map<String, List<StalwartEnumVariant>> enums,
        List<StalwartDashboard> dashboards,
        List<StalwartLayout> layouts) {}
