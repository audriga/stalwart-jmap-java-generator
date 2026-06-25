package com.audriga.stalwartgenerator.model;

import static com.audriga.stalwartgenerator.JmapStalwartGenerator.serializedName;
import static com.google.common.html.HtmlEscapers.htmlEscaper;

import com.audriga.stalwartgenerator.Context;
import com.palantir.javapoet.TypeSpec;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public record GenEnum(String schemaName, String javaName, Stream<GenEnumVariant> variants) implements GenClass {
    @Override
    public TypeSpec.Builder generate(Context ctx) {
        var enumBuilder = TypeSpec.enumBuilder(javaName).addModifiers(Modifier.PUBLIC);
        variants.forEach(variant -> {
            var builder = TypeSpec.anonymousClassBuilder("")
                    .addAnnotation(serializedName(variant.schemaName()))
                    .addJavadoc("$L", htmlEscaper().escape(variant.label()));
            if (variant.explanation() != null) {
                builder.addJavadoc("""

                        <p>
                        $L
                        """, htmlEscaper().escape(variant.explanation()));
            }
            enumBuilder.addEnumConstant(variant.javaName(), builder.build());
        });
        return enumBuilder;
    }
}
