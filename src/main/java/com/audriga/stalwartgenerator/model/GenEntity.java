package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.TypeSpec;
import rs.ltt.jmap.annotation.JmapEntity;

public record GenEntity(
        String description,
        String permissionPrefix,
        boolean enterprise,
        GenSchemaType type) implements GenClass {
    @Override
    public String schemaName() {
        return type.schemaName();
    }

    @Override
    public String javaName() {
        return type.javaName();
    }

    @Override
    public TypeSpec generate(Context ctx) {
        var builder = type.generate(ctx).toBuilder()
                .addAnnotation(AnnotationSpec
                        .builder(JmapEntity.class)
                        .addMember("name", "$S", schemaName())
                        .build())
                .addJavadoc("""
                        $L
                        <p>
                        permission prefix: $L
                        """, description, permissionPrefix);
        if (enterprise) {
            builder.addJavadoc("<br>enterprise: true");
        }
        return builder.build();
    }
}
