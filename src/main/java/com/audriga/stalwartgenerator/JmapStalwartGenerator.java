package com.audriga.stalwartgenerator;

import com.audriga.stalwartgenerator.gson.SealedTypeAdapterFactory;
import com.audriga.stalwartgenerator.schema.StalwartEnumVariant;
import com.audriga.stalwartgenerator.schema.StalwartFieldType;
import com.audriga.stalwartgenerator.schema.StalwartFields;
import com.audriga.stalwartgenerator.schema.StalwartSchema;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.palantir.javapoet.*;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        var ctx = new Context(config.pkg());

        for (var entry : schema.fields().entrySet()) {
            var type = generateStruct(ctx, entry.getKey(), entry.getValue());
            JavaFile.builder(config.pkg(), type).build().writeTo(srcDir);
        }
        for (var entry : schema.enums().entrySet()) {
            var type = generateEnum(ctx, entry.getKey(), entry.getValue());
            JavaFile.builder(config.pkg(), type).build().writeTo(srcDir);
        }
    }

    private static TypeSpec generateStruct(Context ctx, String name, StalwartFields fields) {
        var className = ctx.jmapToClass(name);
        var recordSpec = TypeSpec
                .recordBuilder(className)
                .addModifiers(Modifier.PUBLIC);
        var builderSpec = TypeSpec
                .classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        var recordCtor = MethodSpec.constructorBuilder();
        fields.toModel(ctx).forEach(field -> {
            var paramSpec = ParameterSpec.builder(field.typeName(), field.javaName());
            if (field.nullable()) {
                paramSpec.addAnnotation(Nullable.class);
            }
            if (!field.name().equals(field.javaName())) {
                paramSpec.addAnnotation(serializedName(field.name()));
            }
            recordCtor.addParameter(paramSpec.build());

            var builderField = FieldSpec.builder(field.typeName(), field.javaName(), Modifier.PRIVATE);
            if (!field.typeName().isPrimitive()) builderField.addAnnotation(Nullable.class);
            var builderMethod = MethodSpec
                    .methodBuilder(field.javaName())
                    .returns(ClassName.get(ctx.pkg(), className, "Builder"))
                    .addParameter(field.typeName(), "value")
                    .addStatement("this.$L = value", field.javaName())
                    .addStatement("return this")
                    .build();
            builderSpec.addField(builderField.build()).addMethod(builderMethod);
        });
        builderSpec.addMethod(MethodSpec
                .methodBuilder("build")
                .addStatement("return new $T()", ClassName.get(ctx.pkg(), className))
                .build());
        return recordSpec
                .recordConstructor(recordCtor.build())
                .addType(builderSpec.build())
                .build();
    }

    private static TypeSpec generateEnum(Context ctx, String name, List<StalwartEnumVariant> variants) {
        var enumBuilder = TypeSpec.enumBuilder(name).addModifiers(Modifier.PUBLIC);
        variants.stream().map(v -> v.toModel(ctx)).forEach(variant -> {
            var builder = TypeSpec.anonymousClassBuilder("")
                    .addAnnotation(serializedName(variant.jmapName()))
                    .addJavadoc("$L", variant.label());
            if (variant.explanation() != null) {
                builder.addJavadoc("""
                        
                        <p>
                        $L
                        """, variant.explanation());
            }
            enumBuilder.addEnumConstant(variant.javaName(), builder.build());
        });
        return enumBuilder.build();
    }

    private static AnnotationSpec serializedName(String value) {
        return AnnotationSpec.builder(SerializedName.class).addMember("value", "$S", value).build();
    }
}
