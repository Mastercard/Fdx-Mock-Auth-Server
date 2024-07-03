package com.mastercard.fdx.mock.oauth2.server.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class MTLSConfig {

	private MTLSConfig() {
	}

	public static HttpClient createHttpClient(String trustStorePath, String trustStorePass, String keyStorePath, String keyStorePass)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException, KeyManagementException {

		KeyStore trustStore;
		KeyStore keyStore;

		trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

		trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePass.toCharArray());

		keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		keyStore.load(new FileInputStream(ResourceUtils.getFile(keyStorePath)), keyStorePass.toCharArray());

		Key pk = keyStore.getKey("1", "changeit".toCharArray());

		SSLContext sslContext = new SSLContextBuilder().loadKeyMaterial(keyStore, keyStorePass.toCharArray())
				.loadTrustMaterial(trustStore, (x509Certificates, s) -> true).build();

		SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder.create()
				.setSslContext(sslContext)
				.setTlsVersions("TLSv1.2","TLSv1.3")
				.build();

		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(sslConnectionSocketFactory).setMaxConnTotal(50).setMaxConnPerRoute(50).build();

		HttpClient client = HttpClients.custom().setConnectionManager(connectionManager).build();
		return client;
	}

}
