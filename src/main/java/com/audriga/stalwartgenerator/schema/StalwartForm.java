package com.audriga.stalwartgenerator.schema;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record StalwartForm(@Nullable String title, @Nullable String subtitle, List<StalwartFormSection> sections) {}
