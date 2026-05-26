package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.RenameTag;
import com.audriga.stalwartgenerator.gson.TagStyle;
import com.google.common.base.CaseFormat;
import java.util.List;

@RenameTag(CaseFormat.LOWER_CAMEL)
@TagStyle(TagStyle.E.EXTERNAL)
public sealed interface StalwartLayoutItem {
    record Container(String name, String icon, List<StalwartLayoutSubItem> items) implements StalwartLayoutItem {}

    record Link(String name, String icon, String viewName) implements StalwartLayoutItem {}
}
