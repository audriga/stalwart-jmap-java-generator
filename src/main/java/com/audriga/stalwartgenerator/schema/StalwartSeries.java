package com.audriga.stalwartgenerator.schema;

import java.util.List;

public record StalwartSeries(
        String label,
        List<String> metrics,
        StalwartAggregate aggregate) {
}
