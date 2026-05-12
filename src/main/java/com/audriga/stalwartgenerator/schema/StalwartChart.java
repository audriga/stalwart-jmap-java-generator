package com.audriga.stalwartgenerator.schema;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record StalwartChart(
        String title,
        StalwartChartKind kind,
        List<StalwartSeries> series,
        boolean stacked,
        @Nullable StalwartMetricFormat valueFormat,
        @Nullable String description) {
}
