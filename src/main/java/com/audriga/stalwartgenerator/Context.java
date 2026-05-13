package com.audriga.stalwartgenerator;

import com.palantir.javapoet.ClassName;

public record Context(String pkg) {
    public ClassName type(String name) {
        return ClassName.get(pkg, name.replace("x:", "Stalwart"));
    }
}
