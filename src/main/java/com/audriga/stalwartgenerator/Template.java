package com.audriga.stalwartgenerator;

import com.google.common.collect.Streams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Template {
    private static final Pattern VAR_PATTERN = Pattern.compile("@\\((\\p{Alnum}+)\\)");

    public static final Template POM_XML;
    public static final Template PACKAGE_INFO;

    static {
        try {
            POM_XML = new Template("pom.xml");
            PACKAGE_INFO = new Template("src/main/java/@(pkgPath)/package-info.java");
        } catch (IOException e) {
            throw new IllegalStateException("template resource not found", e);
        }
    }

    private final Matcher pathMatcher;
    private final Matcher contentMatcher;
    private final Set<String> variables;

    public Template(String path) throws IOException {
        var resourceName = "/template/" + path;
        String text;
        try (var stream = getClass().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IllegalArgumentException("resource " + resourceName + " not found");
            }
            text = new InputStreamReader(stream, StandardCharsets.UTF_8).readAllAsString();
        }
        pathMatcher = VAR_PATTERN.matcher(path);
        contentMatcher = VAR_PATTERN.matcher(text);
        variables = Streams.concat(pathMatcher.results(), contentMatcher.results())
                .map(res -> res.group(1))
                .collect(Collectors.toSet());
    }

    public void apply(Map<String, String> values, Path baseDir) throws IOException {
        if (!values.keySet().containsAll(variables)) {
            throw new IllegalArgumentException("missing variable values in " + values + " (expected: " + variables + ")");
        }
        var path = pathMatcher.replaceAll(match -> values.get(match.group(1)));
        var content = contentMatcher.replaceAll(match -> values.get(match.group(1)));
        var resolved = baseDir.resolve(path);
        Files.createDirectories(resolved.getParent());
        Files.writeString(resolved, content, StandardOpenOption.CREATE_NEW);
    }
}
