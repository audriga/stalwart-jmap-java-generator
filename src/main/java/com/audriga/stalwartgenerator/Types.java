package com.audriga.stalwartgenerator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import java.util.Map;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public final class Types {
    private Types() {}

    public static final ClassName STRING = ClassName.get(String.class);
    public static final ClassName MAP = ClassName.get(Map.class);

    public static TypeName nullable(TypeName type) {
        // don't annotate twice
        if (type.annotations().stream()
                .map(AnnotationSpec::type)
                .map(ClassName.class::cast)
                .map(ClassName::simpleName)
                .anyMatch(Predicate.isEqual("Nullable"))) return type;

        return type.box().annotated(AnnotationSpec.builder(Nullable.class).build());
    }

    public static ParameterizedTypeName map(TypeName key, TypeName value) {
        return ParameterizedTypeName.get(MAP, key, value);
    }
}
