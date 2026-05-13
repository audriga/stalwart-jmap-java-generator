package com.audriga.stalwartgenerator;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import rs.ltt.jmap.client.http.BearerAuthHttpAuthentication;

import java.io.IOException;

public class SchemaFetchTest {
    @Test
    void fetch() throws IOException {
        var schema = JmapStalwartGenerator.fetchSchema(
                InsecureX509TrustManager.HTTP_CLIENT,
                HttpUrl.get("https://stalwart.luna.test"),
                new BearerAuthHttpAuthentication("admin", "API_AAAAAQAAAAJNCmLgsT9TbFgq0wUacb1nM5iqQA"));
    }
}
