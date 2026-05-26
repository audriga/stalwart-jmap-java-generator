package com.audriga.stalwartgenerator;

import java.nio.file.Path;

public record Config(String schemaVersion, Path baseDir, boolean overwrite, String pkg) {
    public Config(String schemaVersion, Path baseDir, boolean overwrite) {
        this(schemaVersion, baseDir, overwrite, "com.audriga.stalwart");
    }

    public String pkgPath() {
        return pkg.replace('.', '/');
    }
}
