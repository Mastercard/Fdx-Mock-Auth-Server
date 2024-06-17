package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.utils.Jwks;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.BadJWTException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationValidatorServiceTest {
    @Mock
    RemoteJWKSSetHelper remoteJWKSSetHelper;

    @Mock
    ApplicationProperties appProps;

    @InjectMocks
    AuthorizationValidatorService authorizationValidatorService;

    @Test
    void testInvalidAuthorizationHeader() {
        assertThrows(com.nimbusds.oauth2.sdk.ParseException.class, () -> authorizationValidatorService.validate("INVALID_AUTH_TOKEN", "CLIENT_ID"));
    }

    @Test
    void testInvalidAuthorizationBearerToken() {
        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);

        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);

        assertThrows(ParseException.class, () -> authorizationValidatorService.validate("Bearer INVALID_AUTH_TOKEN", "CLIENT_ID"));
    }

    @Test
    void testValidAuthorizationBearerToken() throws Exception {

        String clientId = "c6327f87-687a-4369-99a4-eaacd3bb8210";
        String dhIssuer = "https://mybank.authserver.com";

        List<JWK> jwks = new ArrayList<>();
        JWK dhJwk = Jwks.generateRSAJwk("DHJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);
        jwks.add(dhJwk);

        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);
        when(keySet.get(any(), any())).thenReturn(jwks);

        when(appProps.getAuthServerBaseUrl()).thenReturn(dhIssuer);
        when(appProps.getAuthServerJwksUriPath()).thenReturn("/jwks");
        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);
//        when(authServerService.introspect(any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>("BLAH", HttpStatus.OK));

        String signedAuthToken = generateSignedAccessToken(dhJwk, clientId, dhIssuer);

        authorizationValidatorService.validate("Bearer " + signedAuthToken, clientId);
    }

    @Test
    void testIssuerMismatch() throws RemoteKeySourceException {

        String clientId = "c6327f87-687a-4369-99a4-eaacd3bb8210";
        String dhIssuer = "https://mybank.authserver.com";

        List<JWK> jwks = new ArrayList<>();
        JWK dhJwk = Jwks.generateRSAJwk("DHJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);
        jwks.add(dhJwk);

        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);
        when(keySet.get(any(), any())).thenReturn(jwks);

        when(appProps.getAuthServerBaseUrl()).thenReturn(dhIssuer);
        when(appProps.getAuthServerJwksUriPath()).thenReturn("/jwks");
        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);

        String signedAuthToken = generateSignedAccessToken(dhJwk, clientId, "DIFFERENT_DH_ISSUER");

        assertThrows(BadJWTException.class, () -> authorizationValidatorService.validate("Bearer " + signedAuthToken, clientId));
    }

    @Test
    void testAudienceMismatch() throws RemoteKeySourceException {

        String clientId = "c6327f87-687a-4369-99a4-eaacd3bb8210";
        String dhIssuer = "https://mybank.authserver.com";

        List<JWK> jwks = new ArrayList<>();
        JWK dhJwk = Jwks.generateRSAJwk("DHJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);
        jwks.add(dhJwk);

        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);
        when(keySet.get(any(), any())).thenReturn(jwks);

        when(appProps.getAuthServerBaseUrl()).thenReturn(dhIssuer);
        when(appProps.getAuthServerJwksUriPath()).thenReturn("/jwks");
        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);

        String signedAuthToken = generateSignedAccessToken(dhJwk, "DIFFERENT_CLIENT_ID", dhIssuer);

        assertThrows(BadJWTException.class, () -> authorizationValidatorService.validate("Bearer " + signedAuthToken, clientId));
    }

    private String generateSignedAccessToken(JWK dhJwk, String clientId, String dhIssuer) {
        JSONObject authTokenRequestJson = new JSONObject();
        long iat = Instant.now().getEpochSecond();
        long exp = iat + 3600;
        authTokenRequestJson.put("iat", iat);
        authTokenRequestJson.put("nbf", iat);
        authTokenRequestJson.put("exp", exp);
        authTokenRequestJson.put("jti", "2327b6d6-5c83-40c4-8fe6-f217e8f55d3b");
        authTokenRequestJson.put("iss", dhIssuer);
        authTokenRequestJson.put("aud", clientId);
        authTokenRequestJson.put("sub", clientId);
        return Jwks.sign(dhJwk, authTokenRequestJson.toString());
    }

}
