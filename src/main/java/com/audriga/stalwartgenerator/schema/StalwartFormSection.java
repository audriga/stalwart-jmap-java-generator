package com.audriga.stalwartgenerator.schema;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record StalwartFormSection(@Nullable String title, List<StalwartFormField> fields) {}
