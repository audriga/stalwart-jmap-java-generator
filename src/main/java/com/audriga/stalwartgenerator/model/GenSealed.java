package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public record GenSealed(String schemaName, String javaName,
                        Stream<GenSealed.Variant> variants) implements GenSchemaType {
    public record Variant(String schemaName, String javaName, String label, @Nullable ClassName innerType) {
    }

    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var interfaceBuilder = TypeSpec.interfaceBuilder(javaName).addModifiers(Modifier.PUBLIC, Modifier.SEALED);
        variants.forEach(variant -> {
            var variantBuilder = TypeSpec
                    .recordBuilder(variant.javaName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addSuperinterface(ctx.type(javaName))
                    .addJavadoc("$L", variant.label);
            if (variant.innerType != null) {
                variantBuilder.recordConstructor(MethodSpec
                        .constructorBuilder()
                        .addParameter(variant.innerType, "data")
                        .build());
            }
            interfaceBuilder.addType(variantBuilder.build());
        });
        return interfaceBuilder;
    }
}
