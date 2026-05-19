package com.audriga.stalwartgenerator;

import com.palantir.javapoet.ClassName;

public record Context(String pkg) {
    public ClassName type(String name) {
        return ClassName.get(pkg, jmapToClass(name));
    }

    // not static in case we decide to make the class name prefix configurable
    public String jmapToClass(String name) {
        return name.replace("x:", "Stalwart");
    }

    public String jmapToEnumConstant(String name) {
        final class Acc {
            final StringBuilder builder = new StringBuilder();
            boolean wasStart = true;
        }
        return name.codePoints().collect(Acc::new, (acc, cp) -> {
            switch (cp) {
                case '_', '-', '.' -> {
                    acc.builder.append('_');
                    acc.wasStart = false;
                }
                default -> {
                    var isStart = Character.isUpperCase(cp) || Character.isDigit(cp);
                    if (!acc.wasStart && isStart) {
                        acc.builder.append('_');
                    }
                    acc.builder.appendCodePoint(Character.toUpperCase(cp));
                    acc.wasStart = isStart;
                }
            }
        }, (left, right) -> {
            left.builder.append(right.builder);
            left.wasStart = right.wasStart;
        }).builder.toString();
    }
}
