package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.schema.StalwartSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rs.ltt.jmap.client.http.HttpAuthentication;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public final class JmapStalwartGenerator {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory())
            .create();

    public static StalwartSchema fetchSchema(
            OkHttpClient httpClient,
            HttpUrl baseUrl,
            HttpAuthentication auth) throws IOException {
        var url = Objects.requireNonNull(baseUrl.resolve("/api/schema"));
        var builder = new Request.Builder().url(url);
        auth.authenticate(builder);
        try (var res = httpClient.newCall(builder.build()).execute()) {
            if (!res.isSuccessful()) throw new IOException("schema request was unsuccessful: " + res.code());
            assert res.body() != null;
            return parseSchema(res.body().charStream());
        }
    }

    public static StalwartSchema parseSchema(Reader input) {
        return GSON.fromJson(input, StalwartSchema.class);
    }

    public static void generate(StalwartSchema schema) {
    }
}
