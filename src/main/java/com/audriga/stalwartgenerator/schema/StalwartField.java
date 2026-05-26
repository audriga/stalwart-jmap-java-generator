package com.audriga.stalwartgenerator.schema;

public record StalwartField(
        String description, StalwartFieldType type, StalwartFieldUpdate update, boolean enterprise) {}
