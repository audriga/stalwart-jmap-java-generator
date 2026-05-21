package com.audriga.stalwartgenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Map;
import java.util.regex.Pattern;

/// Simple recursive file templating with variables.
///
/// Both file paths and contents are subject to variable replacement.
/// The Syntax is `@(variableName)`.
/// Variables without a given replacement are treated as an error.
public class Template {
    public static final Template BUNDLED = Template.ofResource("/template", Template.class);

    private static final Pattern VAR_PATTERN = Pattern.compile("@\\((\\p{Alnum}+)\\)");

    private final URI uri;

    public Template(URI uri) {
        this.uri = uri;
    }

    public static Template ofResource(String name, Class<?> clazz) {
        var url = clazz.getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("resource " + name + " does not exist");
        }
        try {
            return new Template(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError("URL from getResource is not formatted correctly", e);
        }
    }

    public void apply(Map<String, String> values, Path outDir) throws IOException {
        // In development mode with resources from the system FS, use the existing FileSystem.
        // When running from a packaged Jar, a new FileSystem instance needs to be created.
        try {
            doApply(Path.of(uri), values, outDir);
        } catch (FileSystemNotFoundException e) {
            try (var fs = FileSystems.newFileSystem(uri, Map.of())) {
                doApply(fs.provider().getPath(uri), values, outDir);
            }
        }
    }

    private void doApply(Path root, Map<String, String> values, Path outDir) throws IOException {
        try (var stream = Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
            stream.forEach(path -> {
                try {
                    handlePath(root, path, values, outDir);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private void handlePath(Path root, Path path, Map<String, String> values, Path outDir) throws IOException {
        var out = outDir;
        for (var part : root.relativize(path)) {
            out = out.resolve(replaceVars(part.toString(), values));
        }
        if (Files.isDirectory(path)) {
            Files.createDirectories(out);
        } else {
            var text = Files.readString(path);
            Files.writeString(out, replaceVars(text, values), StandardOpenOption.CREATE_NEW);
        }
    }

    private String replaceVars(String template, Map<String, String> values) {
        return VAR_PATTERN.matcher(template).replaceAll(match -> {
            var variable = match.group(1);
            var value = values.get(variable);
            if (value == null) {
                throw new IllegalArgumentException("missing value for variable " + variable);
            }
            return value;
        });
    }
}
