package com.audriga.stalwartgenerator;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import rs.ltt.jmap.client.Services;
import rs.ltt.jmap.client.http.BasicAuthHttpAuthentication;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaFetchTest {
    @Test
    void fetch() throws IOException {
        try (var stalwart = new GenericContainer<>("stalwartlabs/stalwart:v0.16.5")
                .withExposedPorts(8080)
                .withEnv("STALWART_RECOVERY_ADMIN", "admin:pw")) {
            stalwart.start();
            var schema = JmapStalwartGenerator.fetchSchema(
                    Services.okHttpClient(),
                    HttpUrl.get("http://%s:%d".formatted(stalwart.getHost(), stalwart.getFirstMappedPort())),
                    new BasicAuthHttpAuthentication("admin", "pw"));
            assertTrue(schema.objects().containsKey("x:Account"));
        }
    }
}
