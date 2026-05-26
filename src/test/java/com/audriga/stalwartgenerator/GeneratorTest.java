package com.audriga.stalwartgenerator;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class GeneratorTest {
    @Test
    void generate() throws IOException {
        JmapStalwartGenerator.generate(new Config(Path.of("gen"), true), JmapStalwartGenerator.bundledSchema());
    }
}
