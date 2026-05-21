package com.audriga.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FlattenTypeAdapterFactory implements TypeAdapterFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        var raw = type.getRawType();
        var components = raw.getRecordComponents();
        if (components == null || components.length != 1) return null;
        if (Annotations.getRecursive(raw, Flatten.class).filter(Flatten::value).isEmpty()) return null;

        var component = components[0];
        @SuppressWarnings("unchecked")
        var delegate = (TypeAdapter<Object>) gson.getDelegateAdapter(this, TypeToken.get(component.getGenericType()));
        MethodHandle accessor;
        MethodHandle ctor;
        try {
            accessor = LOOKUP.unreflect(component.getAccessor());
            ctor = LOOKUP.findConstructor(raw, MethodType.methodType(void.class, component.getType()));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                Object inner;
                try {
                    inner = accessor.invoke(value);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                delegate.write(out, inner);
            }

            @Override
            @SuppressWarnings("unchecked")
            public T read(JsonReader in) throws IOException {
                var inner = delegate.read(in);
                try {
                    return (T) ctor.invoke(inner);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
