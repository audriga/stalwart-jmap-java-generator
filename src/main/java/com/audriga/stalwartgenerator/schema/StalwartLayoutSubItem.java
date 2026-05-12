package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import java.util.List;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartLayoutSubItem {
    record Container(
            String name,
            List<StalwartLayoutSubItem> items) implements StalwartLayoutSubItem {
    }

    record Link(String name, String viewName) implements StalwartLayoutSubItem {
    }
}
