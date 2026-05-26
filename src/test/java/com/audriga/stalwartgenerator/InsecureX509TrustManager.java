package com.audriga.stalwartgenerator;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import rs.ltt.jmap.client.Services;

public class InsecureX509TrustManager implements X509TrustManager {
    public static final InsecureX509TrustManager INSTANCE = new InsecureX509TrustManager();
    public static final OkHttpClient HTTP_CLIENT = Services.okHttpClient(INSTANCE)
            .newBuilder()
            .hostnameVerifier((hostname, session) ->
                    hostname.endsWith(".test") || OkHostnameVerifier.INSTANCE.verify(hostname, session))
            .build();

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
