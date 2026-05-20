package com.audriga.stalwartgenerator.schema;

import com.audriga.stalwartgenerator.Context;
import com.audriga.stalwartgenerator.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record StalwartSchema(
        Map<String, StalwartObjectType> objects,
        Map<String, StalwartObjectSchema> schemas,
        Map<String, StalwartFields> fields,
        Map<String, StalwartForm> forms,
        Map<String, StalwartList> lists,
        Map<String, List<StalwartEnumVariant>> enums,
        List<StalwartDashboard> dashboards,
        List<StalwartLayout> layouts) {
    public Stream<GenClass> toModel(Context ctx) {
        var schemaModels = schemas.entrySet().stream().flatMap(e -> {
            if (!e.getKey().startsWith("x:")) return Stream.of();
            return switch (e.getValue()) {
                case StalwartObjectSchema.Multiple multiple -> Stream.of(new GenSealed(
                        e.getKey(),
                        ctx.jmapToClass(e.getKey()),
                        multiple.variants().stream().map(v -> v.toModel(ctx))));
                case StalwartObjectSchema.Single single -> Stream.of(new GenStruct(
                        e.getKey(),
                        ctx.jmapToClass(e.getKey()),
                        fields.get(single.schemaName()).toModel(ctx)));
            };
        }).map(typeModel -> switch (objects.get(typeModel.schemaName())) {
            case StalwartObjectType.Real r ->
                    new GenEntity(r.description(), r.permissionPrefix(), r.enterprise(), typeModel);
            case null, default -> typeModel;
        });
        var enumModels = enums.entrySet().stream().map(e -> new GenEnum(
                e.getKey(),
                ctx.jmapToEnum(e.getKey()),
                e.getValue().stream().map(v -> v.toModel(ctx))));
        return Stream.concat(schemaModels, enumModels);
    }
}
