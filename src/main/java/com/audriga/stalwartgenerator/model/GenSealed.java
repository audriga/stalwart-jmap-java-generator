package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.palantir.javapoet.*;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.annotation.Immutable;
import rs.ltt.jmap.annotation.Inline;
import rs.ltt.jmap.annotation.ServerSet;
import rs.ltt.jmap.annotation.Type;

public record GenSealed(
        String schemaName,
        String javaName,
        Stream<GenSealed.Variant> variants,
        @Nullable EntityInfo entityInfo) implements GenSchemaType {
    public record Variant(
            String schemaName,
            String javaName,
            String label,
            @Nullable ClassName innerType) {}

    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var interfaceBuilder = TypeSpec.interfaceBuilder(javaName)
                .addModifiers(Modifier.PUBLIC, Modifier.SEALED)
                .addAnnotation(Type.class);
        if (entityInfo != null) {
            entityInfo.apply(ctx, interfaceBuilder, this);
        }
        variants.forEach(variant -> {
            var variantBuilder = TypeSpec.recordBuilder(variant.javaName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addSuperinterface(ctx.type(javaName))
                    .addJavadoc("$L", variant.label)
                    .addAnnotation(AnnotationSpec.builder(Type.class)
                            .addMember("value", "$S", variant.schemaName)
                            .build());
            var recordCtor = MethodSpec.constructorBuilder();
            if (entityInfo != null) {
                recordCtor.addParameter(ParameterSpec.builder(Types.nullable(Types.STRING), "id")
                        .addAnnotation(Override.class)
                        .addAnnotation(Immutable.class)
                        .addAnnotation(ServerSet.class)
                        .build());
            }
            if (variant.innerType != null) {
                recordCtor.addParameter(ParameterSpec.builder(variant.innerType, "data")
                        .addAnnotation(Inline.class)
                        .build());
            }
            interfaceBuilder.addType(
                    variantBuilder.recordConstructor(recordCtor.build()).build());
        });
        return interfaceBuilder;
    }
}
