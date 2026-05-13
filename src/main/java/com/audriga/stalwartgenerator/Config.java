package com.audriga.stalwartgenerator;

import java.nio.file.Path;

public record Config(Path baseDir, boolean overwrite, String pkg) {
    public Config(Path baseDir, boolean overwrite) {
        this(baseDir, overwrite, "com.audriga.stalwart");
    }

    public String pkgPath() {
        return pkg.replace('.', '/');
    }
}
