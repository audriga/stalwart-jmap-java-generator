package com.audriga.stalwartgenerator.model;

import com.audriga.jmap.gson.FieldMutability;
import com.audriga.jmap.gson.Flatten;
import com.audriga.jmap.gson.Mutability;
import com.audriga.jmap.gson.Tag;
import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.palantir.javapoet.*;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public record GenSealed(
        String schemaName,
        String javaName,
        Stream<GenSealed.Variant> variants,
        @Nullable EntityInfo entityInfo) implements GenSchemaType {
    public record Variant(String schemaName, String javaName, String label, @Nullable ClassName innerType) {
    }

    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var interfaceBuilder = TypeSpec.interfaceBuilder(javaName).addModifiers(Modifier.PUBLIC, Modifier.SEALED);
        if (entityInfo != null) {
            entityInfo.apply(ctx, interfaceBuilder, this);
        }
        variants.forEach(variant -> {
            var variantBuilder = TypeSpec
                    .recordBuilder(variant.javaName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addSuperinterface(ctx.type(javaName))
                    .addJavadoc("$L", variant.label);
            if (!variant.schemaName.equals(variant.javaName)) {
                variantBuilder.addAnnotation(AnnotationSpec
                        .builder(Tag.class)
                        .addMember("value", "$S", variant.schemaName)
                        .build());
            }
            var recordCtor = MethodSpec.constructorBuilder();
            if (entityInfo != null) {
                recordCtor.addParameter(ParameterSpec
                        .builder(Types.nullable(Types.STRING), "id")
                        .addAnnotation(Override.class)
                        .addAnnotation(AnnotationSpec
                                .builder(FieldMutability.class)
                                .addMember("value", "$T.$L", Mutability.class, Mutability.SERVER_SET)
                                .build())
                        .build());
            }
            if (variant.innerType != null) {
                recordCtor.addParameter(ParameterSpec
                        .builder(variant.innerType, "data")
                        .addAnnotation(Flatten.class)
                        .build());
            }
            interfaceBuilder.addType(variantBuilder
                    .recordConstructor(recordCtor.build())
                    .build());
        });
        return interfaceBuilder;
    }
}
