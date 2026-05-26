package com.audriga.stalwartgenerator.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface TagStyle {
    E DEFAULT = E.EXTERNAL;

    E value();

    enum E {
        EXTERNAL,
        INTERNAL
    }
}
