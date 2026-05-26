package com.audriga.stalwartgenerator.schema;

import com.audriga.jmap.gson.RenameTag;
import com.google.common.base.CaseFormat;
import java.util.List;

@RenameTag(CaseFormat.LOWER_CAMEL)
public sealed interface StalwartLayoutSubItem {
    record Container(
            String name,
            List<StalwartLayoutSubItem> items) implements StalwartLayoutSubItem {
    }

    record Link(String name, String viewName) implements StalwartLayoutSubItem {
    }
}
