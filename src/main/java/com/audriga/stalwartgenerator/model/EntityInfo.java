package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.Types;
import com.palantir.javapoet.*;
import rs.ltt.jmap.annotation.JmapEntity;
import rs.ltt.jmap.annotation.JmapMethod;
import rs.ltt.jmap.common.Request;
import rs.ltt.jmap.common.entity.Comparator;
import rs.ltt.jmap.common.entity.SetError;
import rs.ltt.jmap.common.entity.filter.Filter;
import rs.ltt.jmap.common.method.MethodCall;
import rs.ltt.jmap.common.method.MethodResponse;
import rs.ltt.jmap.common.method.call.standard.GetMethodCall;
import rs.ltt.jmap.common.method.call.standard.QueryMethodCall;
import rs.ltt.jmap.common.method.call.standard.SetMethodCall;
import rs.ltt.jmap.common.method.response.standard.GetMethodResponse;
import rs.ltt.jmap.common.method.response.standard.QueryMethodResponse;
import rs.ltt.jmap.common.method.response.standard.SetMethodResponse;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Function;

public record EntityInfo(
        String description,
        String permissionPrefix,
        boolean enterprise) {
    public void apply(Context ctx, TypeSpec.Builder builder, GenSchemaType schemaType) {
        var selfType = ClassName.get(ctx.pkg(), schemaType.javaName());
        builder
                .addSuperinterface(ClassName.get(ctx.pkg(), "StalwartIdentifiable"))
                .addAnnotation(AnnotationSpec
                        .builder(JmapEntity.class)
                        .addMember("name", "$S", schemaType.schemaName())
                        .build())
                .addJavadoc("""
                        $L
                        <p>
                        permission prefix: $L
                        """, description, permissionPrefix);
        if (enterprise) {
            builder.addJavadoc("<br>enterprise: true");
        }
        for (var method : Method.values()) {
            builder.addType(method.generate(schemaType, selfType));
        }
    }

    private enum Method {
        GET(
                GetMethodCall.class,
                _ -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.nullable(ArrayTypeName.of(Types.STRING)), "ids"),
                        p(Types.nullable(ArrayTypeName.of(Types.STRING)), "properties"),
                        p(Types.nullable(ClassName.get(Request.Invocation.ResultReference.class)), "idsReference")),
                GetMethodResponse.class,
                entityClass -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.STRING, "state"),
                        p(ArrayTypeName.of(Types.STRING), "notFound"),
                        p(ArrayTypeName.of(entityClass), "list"))),
        SET(
                SetMethodCall.class,
                entityClass -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.nullable(Types.STRING), "ifInState"),
                        p(Types.nullable(Types.map(Types.STRING, entityClass)), "create"),
                        p(Types.nullable(Types.map(Types.STRING, Types.map(Types.STRING, ClassName.OBJECT))), "update"),
                        p(Types.nullable(ArrayTypeName.of(Types.STRING)), "destroy"),
                        p(Types.nullable(ClassName.get(Request.Invocation.ResultReference.class)), "destroyReference")),
                SetMethodResponse.class,
                entityClass -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.STRING, "oldState"),
                        p(Types.STRING, "newState"),
                        p(Types.map(Types.STRING, entityClass), "created"),
                        p(Types.map(Types.STRING, entityClass), "updated"),
                        p(ArrayTypeName.of(Types.STRING), "destroyed"),
                        p(Types.map(Types.STRING, ClassName.get(SetError.class)), "notCreated"),
                        p(Types.map(Types.STRING, ClassName.get(SetError.class)), "notUpdated"),
                        p(Types.map(Types.STRING, ClassName.get(SetError.class)), "notDestroyed")
                )),
        QUERY(
                QueryMethodCall.class,
                entityClass -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.nullable(ParameterizedTypeName.get(ClassName.get(Filter.class), entityClass)), "filter"),
                        p(Types.nullable(ArrayTypeName.of(Comparator.class)), "sort"),
                        p(Types.nullable(TypeName.LONG), "position"),
                        p(Types.nullable(Types.STRING), "anchor"),
                        p(Types.nullable(TypeName.LONG), "anchorOffset"),
                        p(Types.nullable(TypeName.LONG), "limit"),
                        p(Types.nullable(TypeName.BOOLEAN), "calculateTotal")),
                QueryMethodResponse.class,
                _ -> List.of(
                        p(Types.STRING, "accountId"),
                        p(Types.STRING, "queryState"),
                        p(TypeName.BOOLEAN, "canCalculateChanges"),
                        p(Types.nullable(TypeName.LONG), "position"),
                        p(ArrayTypeName.of(Types.STRING), "ids"),
                        p(Types.nullable(TypeName.LONG), "total"),
                        p(Types.nullable(TypeName.LONG), "limit")
                ));

        private final String jmapName;
        private final String className;
        private final ClassName callBaseClass;
        private final Function<ClassName, List<ParameterSpec>> callCtor;
        private final ClassName responseBaseClass;
        private final Function<ClassName, List<ParameterSpec>> responseCtor;

        Method(
                Class<? extends MethodCall> callBaseClass,
                Function<ClassName, List<ParameterSpec>> callCtor,
                Class<? extends MethodResponse> responseBaseClass,
                Function<ClassName, List<ParameterSpec>> responseCtor) {
            jmapName = name().toLowerCase(Locale.ROOT);
            className = Character.toUpperCase(jmapName.charAt(0)) + jmapName.substring(1);
            this.callBaseClass = ClassName.get(callBaseClass);
            this.callCtor = callCtor;
            this.responseBaseClass = ClassName.get(responseBaseClass);
            this.responseCtor = responseCtor;
        }

        public TypeSpec generate(GenSchemaType type, ClassName entityClass) {
            var annotation = AnnotationSpec
                    .builder(JmapMethod.class)
                    .addMember("value", "$S", "%s/%s".formatted(type.schemaName(), jmapName))
                    .build();
            return TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .superclass(ParameterizedTypeName.get(callBaseClass, entityClass))
                    .addAnnotation(annotation)
                    .addMethod(makeCtor(callCtor, entityClass))
                    .addType(TypeSpec
                            .classBuilder("Response")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .superclass(ParameterizedTypeName.get(responseBaseClass, entityClass))
                            .addAnnotation(annotation)
                            .addMethod(makeCtor(responseCtor, entityClass))
                            .build())
                    .build();
        }

        private MethodSpec makeCtor(Function<ClassName, List<ParameterSpec>> params, ClassName entityClass) {
            var ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
            var superCall = new StringJoiner(", ", "super(", ")");
            for (var param : params.apply(entityClass)) {
                ctor.addParameter(param);
                superCall.add(param.name());
            }
            return ctor.addStatement("$L", superCall.toString()).build();
        }

        private static ParameterSpec p(TypeName type, String name) {
            return ParameterSpec.builder(type, name).build();
        }
    }
}
