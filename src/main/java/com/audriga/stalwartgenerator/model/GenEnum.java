package com.audriga.stalwartgenerator.model;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;

public record GenEnum(String schemaName, String javaName, Stream<GenEnumVariant> variants) implements GenClass {
    @Override
    public TypeSpec generate(Context ctx) {
        var enumBuilder = TypeSpec.enumBuilder(javaName).addModifiers(Modifier.PUBLIC);
        variants.forEach(variant -> {
            var builder = TypeSpec.anonymousClassBuilder("")
                    .addAnnotation(serializedName(variant.schemaName()))
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
}
