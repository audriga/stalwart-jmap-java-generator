package com.audriga.jmap.gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.RecordComponent;
import java.util.Optional;

final class Annotations {
    private Annotations() {}

    static <A extends Annotation> Optional<A> get(AnnotatedElement element, Class<A> annotationClass) {
        return Optional.ofNullable(element.getAnnotation(annotationClass));
    }

    static <A extends Annotation> Optional<A> getRecursive(Class<?> element, Class<A> annotationClass) {
        return get(element, annotationClass)
                .or(() -> get(element.getPackage(), annotationClass))
                .or(() -> get(element.getModule(), annotationClass));
    }

    static <A extends Annotation> Optional<A> getRecursive(RecordComponent element, Class<A> annotationClass) {
        return get(element, annotationClass).or(() -> getRecursive(element.getDeclaringRecord(), annotationClass));
    }
}
