package com.audriga.stalwartgenerator.model;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;
import static com.google.common.html.HtmlEscapers.htmlEscaper;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.google.gson.reflect.TypeToken;
import com.palantir.javapoet.*;
import java.util.StringJoiner;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.annotation.Default;
import rs.ltt.jmap.annotation.Immutable;
import rs.ltt.jmap.annotation.ServerSet;
import rs.ltt.jmap.gson.GsonUtils;

public record GenStruct(
        String schemaName,
        String javaName,
        Stream<GenField> fields,
        @Nullable EntityInfo entityInfo) implements GenSchemaType {
    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var selfClass = ClassName.get(ctx.pkg(), javaName);
        var recordSpec = TypeSpec.recordBuilder(javaName).addModifiers(Modifier.PUBLIC);
        var recordCtor = MethodSpec.constructorBuilder();

        var builderSpec =
                TypeSpec.classBuilder("Builder").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        var buildMethod =
                MethodSpec.methodBuilder("build").addModifiers(Modifier.PUBLIC).returns(selfClass);
        var ctorArgs = new StringJoiner(", ");

        if (entityInfo != null) {
            entityInfo.apply(ctx, recordSpec, this);
            recordCtor.addParameter(ParameterSpec.builder(Types.nullable(Types.STRING), "id")
                    .addAnnotation(Override.class)
                    .addAnnotation(Immutable.class)
                    .addAnnotation(ServerSet.class)
                    .build());
            builderSpec.addField(FieldSpec.builder(String.class, "id", Modifier.PRIVATE)
                    .addAnnotation(Nullable.class)
                    .build());
            builderSpec.addMethod(builderMethod(ctx, "id", ClassName.get(String.class)));
            ctorArgs.add("id");
        }
        fields.forEach(field -> {
            var paramSpec = ParameterSpec.builder(field.typeName(), field.javaName())
                    .addJavadoc("$L", htmlEscaper().escape(field.description()));
            if (!field.schemaName().equals(field.javaName())) {
                paramSpec.addAnnotation(serializedName(field.schemaName()));
            }
            switch (field.update()) {
                case immutable -> paramSpec.addAnnotation(Immutable.class);
                case serverSet -> paramSpec.addAnnotation(ServerSet.class);
            }
            if (field.defaultValue() != null) {
                paramSpec.addAnnotation(AnnotationSpec.builder(Default.class)
                        .addMember("value", "$S", field.defaultValue().toString())
                        .build());
            }
            if (field.enterprise()) {
                paramSpec.addJavadoc(" (enterprise)");
            }
            recordCtor.addParameter(paramSpec.addJavadoc("\n").build());

            var builderField = FieldSpec.builder(Types.nullable(field.typeName()), field.javaName(), Modifier.PRIVATE);
            builderSpec
                    .addField(builderField.build())
                    .addMethod(builderMethod(ctx, field.javaName(), field.typeName()));
            if (field.defaultValue() != null) {
                var type = field.typeName();
                var typeArg = type.isPrimitive() || type instanceof ClassName
                        ? CodeBlock.of("$T.class", type.withoutAnnotations())
                        : CodeBlock.of("new $T<$T>() {}", TypeToken.class, type);
                buildMethod.addCode(
                        """
                        if ($L == null) {
                            $L = $T.REGULAR_GSON.fromJson($S, $L);
                        }
                        """,
                        field.javaName(),
                        field.javaName(),
                        GsonUtils.class,
                        field.defaultValue().toString(),
                        typeArg);
            } else if (!field.nullable()) {
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
        return recordSpec.recordConstructor(recordCtor.build()).addType(builderSpec.build());
    }

    private MethodSpec builderMethod(Context ctx, String fieldName, TypeName fieldType) {
        return MethodSpec.methodBuilder(fieldName)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(ctx.pkg(), javaName, "Builder"))
                .addParameter(fieldType, "value")
                .addStatement("this.$L = value", fieldName)
                .addStatement("return this")
                .build();
    }
}
