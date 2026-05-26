package com.audriga.jmap.gson;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public class SealedTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        var raw = type.getRawType();

        var subclasses = permittedSubclasses(raw);
        if (subclasses == null) return null;

        var tagConverter = Annotations.getRecursive(raw, RenameTag.class)
                .map(format -> CaseFormat.UPPER_CAMEL.converterTo(format.value()))
                .orElse(Converter.identity());
        var tags = subclasses.stream()
                .collect(ImmutableBiMap.toImmutableBiMap(
                        Function.identity(),
                        c -> Annotations.get(c, Tag.class)
                                .map(Tag::value)
                                .filter(Predicate.not(String::isBlank))
                                .orElseGet(() -> Objects.requireNonNull(tagConverter.convert(c.getSimpleName())))));

        var tagStyle = Annotations.getRecursive(raw, TagStyle.class)
                .map(TagStyle::value)
                .orElse(TagStyle.DEFAULT);
        var tagField = tagStyle == TagRepr.INTERNAL
                ? Annotations.getRecursive(raw, TagField.class)
                        .map(TagField::value)
                        .orElse(TagField.DEFAULT)
                : null;

        var delegates = subclasses.stream()
                .collect(ImmutableMap.toImmutableMap(
                        Function.identity(), c -> gson.getDelegateAdapter(this, TypeToken.get(c))));

        var jsonElementAdapter = gson.getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                var clazz = value.getClass();
                @SuppressWarnings("unchecked")
                TypeAdapter<T> delegate = (TypeAdapter<T>) delegates.get(clazz);
                if (delegate == null) {
                    throw new JsonParseException("unknown subtype " + clazz.getName() + " of " + raw.getName());
                }

                out.beginObject();
                if (tagStyle == TagRepr.EXTERNAL) {
                    out.name(tags.get(clazz));
                    delegate.write(out, value);
                } else {
                    var object = delegate.toJsonTree(value).getAsJsonObject();
                    if (object.has(tagField)) {
                        throw new JsonParseException("cannot serialize "
                                + clazz.getName()
                                + " because it already defines a field named "
                                + tagField);
                    }
                    out.name(tagField);
                    out.value(tags.get(clazz));
                    for (var entry : object.entrySet()) {
                        out.name(entry.getKey());
                        jsonElementAdapter.write(out, entry.getValue());
                    }
                }
                out.endObject();
            }

            @Override
            public T read(JsonReader in) throws IOException {
                if (tagStyle == TagRepr.EXTERNAL) {
                    in.beginObject();
                    var tag = in.nextName();
                    var value = getDelegate(tag).read(in);
                    in.endObject();
                    return value;
                }

                var object = jsonElementAdapter.read(in).getAsJsonObject();
                if (!object.has(tagField)) {
                    throw new JsonParseException("missing type field '" + tagField + "' for " + object);
                }
                var tag = object.remove(tagField).getAsString();
                return getDelegate(tag).fromJsonTree(object);
            }

            @SuppressWarnings("unchecked")
            private TypeAdapter<T> getDelegate(String tag) {
                var clazz = tags.inverse().get(tag);
                if (clazz == null) {
                    throw new JsonParseException("invalid type tag " + tag + " for sealed class " + raw.getName());
                }
                return (TypeAdapter<T>) delegates.get(clazz);
            }
        }.nullSafe();
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
