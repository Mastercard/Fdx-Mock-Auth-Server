package com.mastercard.fdx.mock.oauth2.server.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.oauth2.sdk.util.tls.TLSUtils;
import com.nimbusds.oauth2.sdk.util.tls.TLSVersion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLSocketFactory;

@Service
@Slf4j
public class AuthorizationValidatorService {
    @Autowired
    private RemoteJWKSSetHelper remoteJWKSSetHelper;

    @Autowired
    private ApplicationProperties appProps;

    private DefaultResourceRetriever defaultResourceRetriever;

    @Value("${server.ssl.trust-store}")
    String trustStorePath;
    @Value("${server.ssl.trust-store-password}")
    String trustStorePass;

    @PostConstruct
    public void setupSSLSocketFactory() throws KeyManagementException, UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore trustStore;
        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream(ResourceUtils.getFile(trustStorePath)), trustStorePass.toCharArray());

        SSLSocketFactory sslSocketFactory =  TLSUtils.createSSLSocketFactory(trustStore, TLSVersion.TLS_1_3);
        defaultResourceRetriever = new DefaultResourceRetriever(
                RemoteJWKSet.resolveDefaultHTTPConnectTimeout(),
                RemoteJWKSet.resolveDefaultHTTPReadTimeout(),
                RemoteJWKSet.resolveDefaultHTTPSizeLimit(),
                true,
                sslSocketFactory);
    }

    public void validate(String authorisation, String clientId) throws ParseException, BadJOSEException, JOSEException, com.nimbusds.oauth2.sdk.ParseException {
        BearerAccessToken authToken = BearerAccessToken.parse(authorisation);

        JWTClaimsSetVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder()
                        .issuer(appProps.getAuthServerBaseUrl())
                        .audience(clientId)
                        .subject(clientId)
                        .build(),
                // names of required claims
                new HashSet<>(Collections.emptyList())
        );

        validateJwt(authToken.getValue(), claimsVerifier);
    }

    public void validateAccessToken(String authorisation)
            throws BadJOSEException, ParseException, JOSEException, com.nimbusds.oauth2.sdk.ParseException {

        JWTClaimsSetVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder()
                        .issuer(appProps.getAuthServerBaseUrl())
                        .build(),
                // names of required claims
                new HashSet<>(Collections.emptyList())
        );

        validateJwt(authorisation, claimsVerifier);
    }

    private void validateJwt(String authToken, JWTClaimsSetVerifier<SecurityContext> claimsVerifier)
            throws ParseException, BadJOSEException, JOSEException {
        RemoteJWKSet<SecurityContext> keySource = remoteJWKSSetHelper.getRemoteJWKSet(appProps.getAuthServerBaseUrl() + appProps.getAuthServerJwksUriPath(), defaultResourceRetriever);

        JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.PS256, keySource);

        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, new JOSEObjectType("at+jwt")));
        jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.process(authToken, null);
    }

}