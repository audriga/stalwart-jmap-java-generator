package com.audriga.stalwartgenerator;

import com.palantir.javapoet.ClassName;

import javax.lang.model.SourceVersion;

public record Context(String pkg) {
    public ClassName type(String name) {
        return ClassName.get(pkg, jmapToClass(name));
    }

    public ClassName enumType(String name) {
        return ClassName.get(pkg, jmapToEnum(name));
    }

    public String escapeName(String name) {
        if (SourceVersion.isName(name)) return name;
        if (Character.isJavaIdentifierStart(name.codePointAt(0))) return name + '_';
        // we assume reason is always reserved keyword, not special characters in name
        return '_' + name;
    }

    // not static in case we decide to make the name generation configurable
    public String jmapToClass(String name) {
        return name.replace("x:", "Stalwart");
    }

    public String jmapToEnum(String name) {
        return "Stalwart" + name;
    }

    public String jmapToEnumConstant(String name) {
        enum State {BEGIN, IN_UPPER, IN_WORD, IN_NUMBER}
        final class Acc {
            final StringBuilder builder = new StringBuilder();
            State state = State.BEGIN;
        }
        var res = name.codePoints().collect(Acc::new, (acc, cp) -> {
            if (cp == '_' || !Character.isJavaIdentifierPart(cp)) {
                if (acc.state != State.BEGIN) {
                    acc.builder.append('_');
                }
                acc.state = State.BEGIN;
                return;
            }
            if (Character.isUpperCase(cp)) {
                if (acc.state != State.IN_UPPER) {
                    acc.builder.append('_');
                }
                acc.state = State.IN_UPPER;
            } else if (Character.isDigit(cp)) {
                if (acc.state != State.IN_NUMBER) {
                    acc.builder.append('_');
                }
                acc.state = State.IN_NUMBER;
            } else {
                acc.state = State.IN_WORD;
            }
            acc.builder.appendCodePoint(Character.toUpperCase(cp));
        }, (left, right) -> {
            left.builder.append(right.builder);
            left.state = right.state;
        }).builder.toString();
        return escapeName(res);
    }
}
