package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.gson.ExternalType;
import com.audriga.stalwartgenerator.gson.TypeCaseFormat;
import com.google.common.base.CaseFormat;
import java.util.List;

@TypeCaseFormat(CaseFormat.LOWER_CAMEL)
@ExternalType
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
