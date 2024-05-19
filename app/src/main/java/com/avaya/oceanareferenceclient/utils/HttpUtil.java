/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package com.avaya.oceanareferenceclient.utils;

import androidx.annotation.NonNull;

import com.avaya.oceanareferenceclient.utils.network.TLSSocketFactory;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class HttpUtil {
    private static final HttpUtil instance = new HttpUtil();

    // create HTTP client
    private OkHttpClient client;

    private HttpUtil() {
        client = buildOkHttpClient(getDefaultTrustManager());
    }

    public static HttpUtil getInstance() {
        return instance;
    }

    public OkHttpClient getHttpClient() {
        return client;
    }

    private static OkHttpClient buildOkHttpClient(X509TrustManager trustManager) {
        try {
            SSLSocketFactory socketFactory = new TLSSocketFactory(new TrustManager[]{ trustManager });

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .sslSocketFactory(socketFactory, trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static X509TrustManager getDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            return trustManager;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException("Unable to acquire default trust managers:"
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

}