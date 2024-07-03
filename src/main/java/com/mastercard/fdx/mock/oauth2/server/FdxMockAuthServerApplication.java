package com.mastercard.fdx.mock.oauth2.server;

import com.mastercard.fdx.mock.oauth2.server.config.MTLSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class FdxMockAuthServerApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(FdxMockAuthServerApplication.class, args);
		log.info("!----FDX Mock Authorization Server Application Started Successfully----!");
	}

	@Bean(name = "CustomRestClient")
	public RestTemplate getRestClient(@Value("${server.ssl.trust-store}") String trustStorePath,
			@Value("${server.ssl.trust-store-password}") String trustStorePass,
			@Value("${server.ssl.client-store}") String keyStorePath,
			@Value("${client-store-password}") String keyStorePass)
			throws UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException,
			IOException, KeyManagementException {
		HttpComponentsClientHttpRequestFactory httpClientFactory = new HttpComponentsClientHttpRequestFactory(MTLSConfig.createHttpClient(trustStorePath, trustStorePass, keyStorePath, keyStorePass));
		RestTemplate restTemplate = new RestTemplate(httpClientFactory);
		return restTemplate;
	}
}
