package com.audriga.stalwartgenerator.schema;

import java.util.List;

public record StalwartLayout(String name, String icon, List<StalwartLayoutItem> items) {}
