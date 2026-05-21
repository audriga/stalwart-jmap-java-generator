package com.audriga.stalwartgenerator.schema;

import com.audriga.gson.TagRepr;
import com.audriga.gson.TagStyle;
import com.audriga.gson.RenameTag;
import com.google.common.base.CaseFormat;
import java.util.List;

@RenameTag(CaseFormat.LOWER_CAMEL)
@TagStyle(TagRepr.EXTERNAL)
public sealed interface StalwartLayoutItem {
    record Container(
            String name,
            String icon,
            List<StalwartLayoutSubItem> items) implements StalwartLayoutItem {
    }

    record Link(
            String name,
            String icon,
            String viewName) implements StalwartLayoutItem {
    }
}
