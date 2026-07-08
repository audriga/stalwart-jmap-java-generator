package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.gson.SealedTypeAdapterFactory;
import com.audriga.stalwartgenerator.schema.StalwartSchema;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.palantir.javapoet.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.audriga.jmap.client.http.HttpAuthentication;

public final class JmapStalwartGenerator {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory())
            .create();
    private static final Object BUNDLED_LOCK = new Object();
    private static volatile StalwartSchema bundled;

    public static StalwartSchema fetchSchema(OkHttpClient httpClient, HttpUrl baseUrl, HttpAuthentication auth)
            throws IOException {
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

    public static StalwartSchema bundledSchema() {
        if (bundled == null) {
            synchronized (BUNDLED_LOCK) {
                if (bundled == null) {
                    try (var stream = JmapStalwartGenerator.class.getResourceAsStream("/schema.json")) {
                        bundled = parseSchema(new InputStreamReader(Objects.requireNonNull(stream)));
                    } catch (Exception e) {
                        throw new IllegalStateException("failed to read bundled schema", e);
                    }
                }
            }
        }
        return bundled;
    }

    public static String bundledVersion() {
        return "0.16.12";
    }

    public static void generate(Config config, StalwartSchema schema) throws IOException {
        if (config.overwrite() && Files.exists(config.baseDir())) {
            // delete everything first for a clean slate without old leftovers
            MoreFiles.deleteRecursively(config.baseDir());
        }
        Files.createDirectories(config.baseDir());

        Template.BUNDLED.apply(
                Map.of(
                        "schemaVersion", config.schemaVersion(),
                        "pkg", config.pkg(),
                        "pkgPath", config.pkgPath()),
                config.baseDir());

        var srcDir = config.baseDir().resolve("src", "main", "java");
        var ctx = new Context(config.pkg());

        ctx.toModel(schema).forEach(classModel -> {
            var type = classModel
                    .generate(ctx)
                    .alwaysQualify("Get", "Set", "Query")
                    .build();
            try {
                JavaFile.builder(ctx.pkg(), type).build().writeTo(srcDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static AnnotationSpec serializedName(String value) {
        return AnnotationSpec.builder(SerializedName.class)
                .addMember("value", "$S", value)
                .build();
    }
}
