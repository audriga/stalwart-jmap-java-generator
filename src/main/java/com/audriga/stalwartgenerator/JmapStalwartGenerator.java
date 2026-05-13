package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.gson.SealedTypeAdapterFactory;
import com.audriga.stalwartgenerator.schema.StalwartSchema;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import lombok.Builder;
import lombok.Getter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.client.http.HttpAuthentication;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

public final class JmapStalwartGenerator {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new SealedTypeAdapterFactory())
            .create();
    private static final Object BUNDLED_LOCK = new Object();
    private static StalwartSchema bundled;

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
        for (var fieldsEntry : schema.fields().entrySet()) {
            var className = fieldsEntry.getKey().replace("x:", "Stalwart");
            var fields = fieldsEntry.getValue();

            var ctx = new Context(config.pkg());
            var typeSpec = TypeSpec
                    .classBuilder(className)
                    .addModifiers(Modifier.FINAL)
                    .addAnnotation(Getter.class);
            for (var prop : fields.properties().entrySet()) {
                var type = prop.getValue().type();

                var name = prop.getKey();
                var isValidName = SourceVersion.isName(name);
                var fieldSpec = FieldSpec.builder(
                        type.toJavaType(ctx),
                        isValidName ? name : name + '_',
                        Modifier.PRIVATE,
                        Modifier.FINAL);
                if (type.nullable()) {
                    fieldSpec.addAnnotation(Nullable.class);
                }
                if (!isValidName) {
                    fieldSpec.addAnnotation(AnnotationSpec.builder(SerializedName.class)
                            .addMember("value", "$S", name)
                            .build());
                }
                var def = fields.defaults().get(prop.getKey());
                if (def != null) {
                    fieldSpec.addAnnotation(Builder.Default.class);
                    // TODO: fieldSpec.initializer(...);
                }

                typeSpec.addField(fieldSpec.build());
            }

            JavaFile.builder(config.pkg(), typeSpec.build()).build().writeTo(srcDir);
        }
    }
}
