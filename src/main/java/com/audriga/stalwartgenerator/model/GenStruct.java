package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.*;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;

public record GenStruct(String schemaName, String javaName, Stream<GenField> fields) implements GenSchemaType {
    @Override
    public TypeSpec generate(Context ctx) {
        var recordSpec = TypeSpec
                .recordBuilder(javaName)
                .addModifiers(Modifier.PUBLIC);
        var builderSpec = TypeSpec
                .classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        var recordCtor = MethodSpec.constructorBuilder();
        fields.forEach(field -> {
            var paramSpec = ParameterSpec.builder(field.typeName(), field.javaName());
            if (field.nullable()) {
                paramSpec.addAnnotation(Nullable.class);
            }
            if (!field.name().equals(field.javaName())) {
                paramSpec.addAnnotation(serializedName(field.name()));
            }
            recordCtor.addParameter(paramSpec.build());

            var builderField = FieldSpec.builder(field.typeName(), field.javaName(), Modifier.PRIVATE);
            if (!field.typeName().isPrimitive()) builderField.addAnnotation(Nullable.class);
            var builderMethod = MethodSpec
                    .methodBuilder(field.javaName())
                    .returns(ClassName.get(ctx.pkg(), javaName, "Builder"))
                    .addParameter(field.typeName(), "value")
                    .addStatement("this.$L = value", field.javaName())
                    .addStatement("return this")
                    .build();
            builderSpec.addField(builderField.build()).addMethod(builderMethod);
        });
        var selfClass = ClassName.get(ctx.pkg(), javaName);
        builderSpec.addMethod(MethodSpec
                .methodBuilder("build")
                .returns(selfClass)
                .addStatement("return new $T()", selfClass)
                .build());
        return recordSpec
                .recordConstructor(recordCtor.build())
                .addType(builderSpec.build())
                .build();
    }
}
