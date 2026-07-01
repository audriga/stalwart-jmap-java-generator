package com.audriga.stalwartgenerator.gson;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.gson.Annotations;
import rs.ltt.jmap.gson.TagRepr;
import rs.ltt.jmap.gson.adapter.SumTypeAdapter;

public class SealedTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        var raw = type.getRawType();
        if (!raw.isSealed()) return null;

        var tagConverter = Annotations.getRecursive(raw, RenameTag.class)
                .map(format -> CaseFormat.UPPER_CAMEL.converterTo(format.value()))
                .orElse(Converter.identity());
        var source = SumTypeAdapter.sealedClassSource(
                raw,
                sub -> {
                    var tag = Annotations.get(sub, Tag.class)
                            .map(Tag::value)
                            .filter(Predicate.not(String::isBlank))
                            .orElseGet(() -> Objects.requireNonNull(tagConverter.convert(sub.getSimpleName())));
                    @SuppressWarnings("unchecked")
                    var adapter = (TypeAdapter<T>) gson.getAdapter(sub);
                    return new SumTypeAdapter.Variant<>(tag, adapter);
                },
                null);
        if (source == null) return null;

        var tagStyle = Annotations.getRecursive(raw, TagStyle.class)
                .map(TagStyle::value)
                .orElse(TagStyle.DEFAULT);
        var tagRepr =
                switch (tagStyle) {
                    case EXTERNAL -> new TagRepr.External();
                    case INTERNAL ->
                        new TagRepr.Internal(
                                Annotations.getRecursive(raw, TagField.class)
                                        .map(TagField::value)
                                        .orElse(TagField.DEFAULT),
                                null);
                };

        return new SumTypeAdapter<>(source, tagRepr, gson.getAdapter(JsonElement.class));
    }

    @Nullable
    private static ImmutableSet<Class<?>> permittedSubclasses(Class<?> clazz) {
        var subclasses = clazz.getPermittedSubclasses();
        // not a sealed class, invalid for this adapter
        if (subclasses == null) return null;
        // we want at least one subclass
        if (subclasses.length == 0) return null;

        for (var sub : subclasses) {
            // open subclass, also invalid
            if (!Modifier.isFinal(sub.getModifiers())) return null;
        }
        return ImmutableSet.copyOf(subclasses);
    }
}
