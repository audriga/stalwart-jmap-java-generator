package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.gson.SealedTypeAdapterFactory;
import com.audriga.stalwartgenerator.schema.StalwartSchema;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.palantir.javapoet.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rs.ltt.jmap.client.http.HttpAuthentication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;

public final class JmapStalwartGenerator {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory())
            .create();
    private static final Object BUNDLED_LOCK = new Object();
    private static volatile StalwartSchema bundled;

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

    public static void generate(Config config, StalwartSchema schema) throws IOException {
        if (config.overwrite()) {
            // delete everything first for a clean slate without old leftovers
            MoreFiles.deleteRecursively(config.baseDir());
        }
        Files.createDirectories(config.baseDir());

        Template.POM_XML.apply(Map.of("schemaVersion", "0.16.5"), config.baseDir());
        Template.PACKAGE_INFO.apply(
                Map.of("pkg", config.pkg(), "pkgPath", config.pkgPath()),
                config.baseDir());
        Files.writeString(config.baseDir().resolve(".gitignore"), """
                target/
                !**/src/main/**/target/
                !**/src/test/**/target/
                """);

        var srcDir = config.baseDir().resolve("src", "main", "java");
        var ctx = new Context(config.pkg());

        ctx.toModel(schema).forEach(classModel -> {
            try {
                JavaFile.builder(ctx.pkg(), classModel.generate(ctx).build()).build().writeTo(srcDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static AnnotationSpec serializedName(String value) {
        return AnnotationSpec.builder(SerializedName.class).addMember("value", "$S", value).build();
    }
}
