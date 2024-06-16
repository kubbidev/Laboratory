package me.kubbidev.laboratory.util.gson.adapter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.kubbidev.laboratory.util.gson.GsonProvider;
import me.kubbidev.laboratory.util.gson.GsonSerializable;

import java.io.IOException;
import java.lang.reflect.Method;

public final class GsonSerializableAdapterFactory implements TypeAdapterFactory {
    public static final GsonSerializableAdapterFactory INSTANCE = new GsonSerializableAdapterFactory();

    private GsonSerializableAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        // also checks if the class can be casted to GsonSerializable
        Method deserializeMethod = GsonSerializable.getDeserializeMethod(clazz);
        if (deserializeMethod == null) {
            return null;
        }

        TypeAdapter<? extends GsonSerializable> typeAdapter = new TypeAdapter<GsonSerializable>() {

            @Override
            public void write(JsonWriter out, GsonSerializable value) {
                if (value == null) {
                    gson.toJson(null, out);
                    return;
                }
                gson.toJson(value.serialize().toJson(), out);
            }

            @Override
            public GsonSerializable read(JsonReader in) throws IOException {
                JsonElement element = GsonProvider.parser().parse(in);

                if (element.isJsonNull()) {
                    return null;
                }
                try {
                    return (GsonSerializable) deserializeMethod.invoke(null, element);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };
        //noinspection unchecked
        return (TypeAdapter<T>) typeAdapter;
    }
}