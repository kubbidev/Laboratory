package me.kubbidev.laboratory.util.gson.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import me.kubbidev.laboratory.util.gson.GsonSerializable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class JArray implements JElement {
    private final JsonArray array = new JsonArray();

    @Override
    public JsonArray toJson() {
        return this.array;
    }

    public JArray add(JsonElement value) {
        if (value == null) {
            return add(JsonNull.INSTANCE);
        }
        this.array.add(value);
        return this;
    }

    public JArray add(GsonSerializable value) {
        if (value == null) {
            return add(JsonNull.INSTANCE);
        }
        return add(value.serialize().toJson());
    }

    public JArray add(String value) {
        if (value == null) {
            return add(JsonNull.INSTANCE);
        }
        return add(new JsonPrimitive(value));
    }

    public JArray addAll(Iterable<String> iterable) {
        for (String s : iterable) {
            add(s);
        }
        return this;
    }

    public JArray add(JElement value) {
        if (value == null) {
            return add(JsonNull.INSTANCE);
        }
        return add(value.toJson());
    }

    public JArray add(Supplier<? extends JElement> value) {
        return add(value.get().toJson());
    }

    public JArray consume(Consumer<? super JArray> consumer) {
        consumer.accept(this);
        return this;
    }
}