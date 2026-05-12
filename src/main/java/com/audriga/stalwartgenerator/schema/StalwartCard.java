package com.audriga.stalwartgenerator.schema;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record StalwartCard(
        String title,
        String icon,
        StalwartCardSource source,
        List<String> metrics,
        StalwartAggregate aggregate,
        StalwartMetricFormat format,
        @Nullable String description,
        boolean sparkline,
        boolean delta
) {
}
