package com.audriga.stalwartgenerator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class GeneratorTest {
    @Test
    void generate() throws IOException {
        JmapStalwartGenerator.generate(new Config(Path.of("gen"), true), JmapStalwartGenerator.bundledSchema());
    }
}
