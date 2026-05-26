package com.audriga.jmap.gson;

import com.google.common.base.CaseFormat;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface RenameTag {
    CaseFormat value();
}
