package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.Jwks;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.oauth2.sdk.PushedAuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.List;

import static com.mastercard.fdx.mock.oauth2.server.utils.PushAuthorizationRequestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PushAuthorizationRequestServiceTest {

    private static JWK jwk1;
    private static JWK jwk2;
    private static String assertionJwt;
    private static String parJwt;

    @Mock
    RemoteJWKSSetHelper remoteJWKSSetHelper;

    @Mock
    RegisteredClientRepository registeredClientRepository;

    @Mock
    PushAuthorizationRequestRepository parRepo;

    @InjectMocks
    private PushedAuthorizationRequestService parService;

    @BeforeAll
    static void setup() {
        jwk1 = Jwks.generateRSAJwk();
        jwk2 = Jwks.generateRSAJwk();

        JSONObject par = generateValidPAR();
        parJwt = Jwks.sign(jwk1, par.toString());

        JSONObject assertion = generateValidAssertion();
        assertionJwt = Jwks.sign(jwk1, assertion.toString());

    }

    @Test
    void testError_InvalidParJwt() {

        String invalidParJwt = Jwks.sign(jwk1, "{\"exp\": \"BAD_VALUE\"}");
        String body = generateValidPARPostData(assertionJwt, invalidParJwt);

        PushedAuthorizationResponse res = parService.processPAR(body);
        assertTrue(res instanceof PushedAuthorizationErrorResponse);
        assertEquals(PushedAuthorizationRequestService.INVALID_PAR_REQUEST, res.toErrorResponse().getErrorObject().getCode());
        assertTrue(res.toErrorResponse().getErrorObject().getDescription().contains("Invalid request parameter: Unexpected type of JSON object member with key exp"));
    }

    @Test
    void testError_InvalidClientId() {

        String body = generateValidPARPostData(assertionJwt, parJwt);

        Mockito.when(registeredClientRepository.findByClientId(Mockito.anyString())).thenReturn(null);

        PushedAuthorizationResponse res = parService.processPAR(body);
        assertTrue(res instanceof PushedAuthorizationErrorResponse);
        assertEquals(PushedAuthorizationRequestService.INVALID_CLIENT, res.toErrorResponse().getErrorObject().getCode());
        assertTrue(res.toErrorResponse().getErrorObject().getDescription().contains("Invalid Client"));
    }

    @Test
    void testError_ValidClientId() throws RemoteKeySourceException {

        String body = generateValidPARPostData(assertionJwt, parJwt);

        ClientSettings clientSettings = ClientSettings.builder().jwkSetUrl("file:///c/temp/jwks1").build();

        RegisteredClient regClient = Mockito.mock(RegisteredClient.class);
        Mockito.when(regClient.getClientSettings()).thenReturn(clientSettings);

        Mockito.when(registeredClientRepository.findByClientId(Mockito.anyString())).thenReturn(regClient);

        RemoteJWKSet<SecurityContext> jwkSet = Mockito.mock(RemoteJWKSet.class);
        Mockito.when(jwkSet.get(Mockito.any(), Mockito.any())).thenReturn(List.of(jwk1));
        Mockito.when(remoteJWKSSetHelper.getRemoteJWKSet(Mockito.anyString())).thenReturn(jwkSet);

        PushedAuthorizationResponse res = parService.processPAR(body);
        assertTrue(res instanceof PushedAuthorizationSuccessResponse);
        assertNotNull(res.toSuccessResponse().toJSONObject().getAsString("request_uri"));
        assertTrue(res.toSuccessResponse().toJSONObject().getAsString("request_uri").contains(PushAuthorizationRequestData.URN_PREFIX));
        assertNotNull(res.toSuccessResponse().toJSONObject().getAsString("expires_in"));
    }

    @Test
    void testError_ValidClientId_ButNotSignedByClient() throws RemoteKeySourceException {

        String body = generateValidPARPostData(assertionJwt, parJwt);

        ClientSettings clientSettings = ClientSettings.builder().jwkSetUrl("file:///c/temp/jwks1").build();

        RegisteredClient regClient = Mockito.mock(RegisteredClient.class);
        Mockito.when(regClient.getClientSettings()).thenReturn(clientSettings);

        Mockito.when(registeredClientRepository.findByClientId(Mockito.anyString())).thenReturn(regClient);

        RemoteJWKSet<SecurityContext> jwkSet = Mockito.mock(RemoteJWKSet.class);
        Mockito.when(jwkSet.get(Mockito.any(), Mockito.any())).thenReturn(List.of(jwk2));
        Mockito.when(remoteJWKSSetHelper.getRemoteJWKSet(Mockito.anyString())).thenReturn(jwkSet);

        PushedAuthorizationResponse res = parService.processPAR(body);

        assertTrue(res instanceof PushedAuthorizationErrorResponse);
        assertEquals(PushedAuthorizationRequestService.INVALID_PAR_REQUEST, res.toErrorResponse().getErrorObject().getCode());
        assertTrue(res.toErrorResponse().getErrorObject().getDescription().contains("Signed JWT rejected: Invalid signature"));
    }


}
