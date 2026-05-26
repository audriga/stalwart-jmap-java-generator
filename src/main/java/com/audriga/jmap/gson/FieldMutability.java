package com.audriga.jmap.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface FieldMutability {
    Mutability DEFAULT = Mutability.MUTABLE;

    Mutability value();
}
