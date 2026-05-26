package com.audriga.stalwartgenerator.model;

import com.audriga.jmap.gson.Default;
import com.audriga.jmap.gson.FieldMutability;
import com.audriga.jmap.gson.Mutability;
import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.palantir.javapoet.*;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;

public record GenStruct(
        String schemaName,
        String javaName,
        Stream<GenField> fields,
        @Nullable EntityInfo entityInfo) implements GenSchemaType {
    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var selfClass = ClassName.get(ctx.pkg(), javaName);
        var recordSpec = TypeSpec
                .recordBuilder(javaName)
                .addModifiers(Modifier.PUBLIC);
        var recordCtor = MethodSpec.constructorBuilder();

        var builderSpec = TypeSpec
                .classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        var buildMethod = MethodSpec.methodBuilder("build").returns(selfClass);
        var ctorArgs = new StringJoiner(", ");

        if (entityInfo != null) {
            entityInfo.apply(ctx, recordSpec, this);
            recordCtor
                    .addParameter(ParameterSpec
                            .builder(Types.nullable(Types.STRING), "id")
                            .addAnnotation(Override.class)
                            .addAnnotation(AnnotationSpec
                                    .builder(FieldMutability.class)
                                    .addMember("value", "$T.$L", Mutability.class, Mutability.SERVER_SET)
                                    .build())
                            .build());
            builderSpec.addField(FieldSpec
                    .builder(String.class, "id")
                    .addAnnotation(Nullable.class)
                    .build());
            builderSpec.addMethod(builderMethod(ctx, "id", ClassName.get(String.class)));
            ctorArgs.add("id");
        }
        fields.forEach(field -> {
            var paramSpec = ParameterSpec.builder(field.typeName(), field.javaName())
                    .addJavadoc("$L", field.description());
            if (!field.schemaName().equals(field.javaName())) {
                paramSpec.addAnnotation(serializedName(field.schemaName()));
            }
            if (field.update().mutability() != FieldMutability.DEFAULT) {
                paramSpec.addAnnotation(AnnotationSpec
                        .builder(FieldMutability.class)
                        .addMember("value", "$T.$L", Mutability.class, field.update().mutability())
                        .build());
            }
            if (field.defaultValue() != null) {
                paramSpec.addAnnotation(AnnotationSpec
                        .builder(Default.class)
                        .addMember("value", "$S", field.defaultValue().toString())
                        .build());
            }
            if (field.enterprise()) {
                paramSpec.addJavadoc(" (enterprise)");
            }
            recordCtor.addParameter(paramSpec.addJavadoc("\n").build());

            var builderField = FieldSpec
                    .builder(Types.nullable(field.typeName()), field.javaName(), Modifier.PRIVATE);
            builderSpec.addField(builderField.build()).addMethod(builderMethod(ctx, field.javaName(), field.typeName()));
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

    private MethodSpec builderMethod(Context ctx, String fieldName, TypeName fieldType) {
        return MethodSpec
                .methodBuilder(fieldName)
                .returns(ClassName.get(ctx.pkg(), javaName, "Builder"))
                .addParameter(fieldType, "value")
                .addStatement("this.$L = value", fieldName)
                .addStatement("return this")
                .build();
    }
}
