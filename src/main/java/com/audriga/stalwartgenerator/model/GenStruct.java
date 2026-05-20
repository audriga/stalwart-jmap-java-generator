package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.*;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;

public record GenStruct(String schemaName, String javaName, Stream<GenField> fields) implements GenSchemaType {
    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var selfClass = ClassName.get(ctx.pkg(), javaName);
        var recordSpec = TypeSpec
                .recordBuilder(javaName)
                .addModifiers(Modifier.PUBLIC);
        var builderSpec = TypeSpec
                .classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        var buildMethod = MethodSpec.methodBuilder("build").returns(selfClass);
        var ctorArgs = new StringJoiner(", ");

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

            var builderField = FieldSpec
                    .builder(field.typeName().box(), field.javaName(), Modifier.PRIVATE)
                    .addAnnotation(Nullable.class);
            var builderMethod = MethodSpec
                    .methodBuilder(field.javaName())
                    .returns(ClassName.get(ctx.pkg(), javaName, "Builder"))
                    .addParameter(field.typeName(), "value")
                    .addStatement("this.$L = value", field.javaName())
                    .addStatement("return this")
                    .build();
            builderSpec.addField(builderField.build()).addMethod(builderMethod);
            if (!field.nullable()) {
                buildMethod.addCode(
                        """
                                if ($L == null) {
                                    throw new $T($S);
                                }
                                """,
                        field.javaName(),
                        IllegalStateException.class,
                        "required field " + field.javaName() + " was not set");
            }
            ctorArgs.add(field.javaName());
        });
        builderSpec.addMethod(buildMethod
                .addStatement("return new $T($L)", selfClass, ctorArgs.toString())
                .build());
        return recordSpec
                .recordConstructor(recordCtor.build())
                .addType(builderSpec.build());
    }
}
