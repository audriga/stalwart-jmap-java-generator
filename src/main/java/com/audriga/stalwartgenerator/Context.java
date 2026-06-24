package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.model.*;
import com.audriga.stalwartgenerator.schema.*;
import com.palantir.javapoet.ClassName;
import java.util.List;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;

public record Context(String pkg) {
    // Names of public or protected methods in Object
    // See also https://docs.oracle.com/javase/specs/jls/se26/html/jls-8.html#jls-8.10.1
    private static final List<String> INVALID_NAMES =
            List.of("clone", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait");

    public ClassName type(String name) {
        return ClassName.get(pkg, jmapToClass(name));
    }

    public ClassName enumType(String name) {
        return ClassName.get(pkg, jmapToEnum(name));
    }

    public String escapeName(String name) {
        if (INVALID_NAMES.contains(name)) return name + '_';
        if (SourceVersion.isName(name)) return name;
        // we assume reason is always reserved keyword, not special characters in schemaName
        if (Character.isJavaIdentifierStart(name.codePointAt(0))) return name + '_';
        return '_' + name;
    }

    // not static in case we decide to make the schemaName generation configurable
    public String jmapToClass(String name) {
        return name.replace("x:", "Stalwart");
    }

    public String jmapToEnum(String name) {
        return "Stalwart" + name;
    }

    public String jmapToEnumConstant(String name) {
        enum State {
            BEGIN,
            IN_UPPER,
            IN_WORD,
            IN_NUMBER
        }
        final class Acc {
            final StringBuilder builder = new StringBuilder();
            State state = State.BEGIN;
        }
        var res = name.codePoints()
                .collect(
                        Acc::new,
                        (acc, cp) -> {
                            if (cp == '_' || !Character.isJavaIdentifierPart(cp)) {
                                if (acc.state != State.BEGIN) {
                                    acc.builder.append('_');
                                }
                                acc.state = State.BEGIN;
                                return;
                            }
                            if (Character.isUpperCase(cp)) {
                                if (acc.state != State.BEGIN && acc.state != State.IN_UPPER) {
                                    acc.builder.append('_');
                                }
                                acc.state = State.IN_UPPER;
                            } else if (Character.isDigit(cp)) {
                                if (acc.state != State.BEGIN && acc.state != State.IN_NUMBER) {
                                    acc.builder.append('_');
                                }
                                acc.state = State.IN_NUMBER;
                            } else {
                                acc.state = State.IN_WORD;
                            }
                            acc.builder.appendCodePoint(Character.toUpperCase(cp));
                        },
                        (left, right) -> {
                            left.builder.append(right.builder);
                            left.state = right.state;
                        })
                .builder
                .toString();
        return escapeName(res);
    }

    public Stream<GenClass> toModel(StalwartSchema schema) {
        var schemaModels = schema.schemas().entrySet().stream()
                .filter(e -> e.getKey().startsWith("x:"))
                .map(e -> {
                    var entityInfo =
                            switch (schema.objects().get(e.getKey())) {
                                case StalwartObjectType.Object o ->
                                    new EntityInfo(o.description(), o.permissionPrefix(), false, o.enterprise());
                                case StalwartObjectType.Singleton s ->
                                    new EntityInfo(s.description(), s.permissionPrefix(), true, s.enterprise());
                                case null, default -> null;
                            };
                    return switch (e.getValue()) {
                        case StalwartObjectSchema.Multiple multiple ->
                            new GenSealed(
                                    e.getKey(),
                                    jmapToClass(e.getKey()),
                                    multiple.variants().stream().map(this::toModel),
                                    entityInfo);
                        case StalwartObjectSchema.Single single ->
                            new GenStruct(
                                    e.getKey(),
                                    jmapToClass(e.getKey()),
                                    toModel(schema.fields().get(single.schemaName())),
                                    entityInfo);
                    };
                });
        var enumModels = schema.enums().entrySet().stream()
                .map(e -> new GenEnum(
                        e.getKey(),
                        jmapToEnum(e.getKey()),
                        e.getValue().stream().map(this::toModel)));
        return Stream.concat(schemaModels, enumModels);
    }

    private GenSealed.Variant toModel(StalwartObjectVariant variant) {
        return new GenSealed.Variant(
                variant.name(),
                escapeName(variant.name()),
                variant.label(),
                variant.schemaName() != null ? type(variant.schemaName()) : null);
    }

    private Stream<GenField> toModel(StalwartFields fields) {
        return fields.properties().entrySet().stream().map(entry -> {
            var name = entry.getKey();
            var javaName = escapeName(name);
            var field = entry.getValue();
            var type = field.type();
            var javaType = type.nullable() ? Types.nullable(type.toJavaType(this)) : type.toJavaType(this);
            return new GenField(
                    name,
                    javaName,
                    field.description(),
                    field.update(),
                    javaType,
                    type.nullable(),
                    fields.defaults().get(name),
                    field.enterprise());
        });
    }

    private GenEnumVariant toModel(StalwartEnumVariant variant) {
        return new GenEnumVariant(
                variant.name(), jmapToEnumConstant(variant.name()), variant.label(), variant.explanation());
    }
}
