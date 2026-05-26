package com.audriga.jmap.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.RECORD_COMPONENT, ElementType.PACKAGE, ElementType.MODULE})
public @interface Flatten {
    boolean value() default true;
}
