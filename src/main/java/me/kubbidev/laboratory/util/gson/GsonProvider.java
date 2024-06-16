package me.kubbidev.laboratory.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import me.kubbidev.laboratory.util.gson.adapter.GsonSerializableAdapterFactory;

public final class GsonProvider {

    private static final Gson NORMAL = new GsonBuilder()
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINTING = new GsonBuilder()
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final JsonParser NORMAL_PARSER = new JsonParser();

    public static JsonParser parser() {
        return NORMAL_PARSER;
    }

    public static Gson normal() {
        return NORMAL;
    }

    public static Gson prettyPrinting() {
        return PRETTY_PRINTING;
    }

    private GsonProvider() {
        throw new AssertionError();
    }

}
