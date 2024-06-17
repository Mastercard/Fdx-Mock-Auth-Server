package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.consent.AccountConsentResponse;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.Jwks;
import com.mastercard.fdx.mock.oauth2.server.utils.OAuth2AuthorizationRequestUtils;
import com.mastercard.fdx.mock.oauth2.server.utils.PushAuthorizationRequestTestUtils;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountConsentServiceTest {

    private final JWK jwk = Jwks.generateRSAJwk();

    private static final String AUTH_PAYLOAD = "{\n" +
            "  \"iat\": 1658878209,\n" +
            "  \"nbf\": 1658878205,\n" +
            "  \"exp\": 1658968209,\n" +
            "  \"jti\": \"7a7db31f-7ecc-4b39-b9f8-790d01e4ff91\",\n" +
            "  \"iss\": \"E660giurm02hp_foRpgZZ0GY5rFszXf1qzqJpiWFtHc\",\n" +
            "  \"sub\": \"E660giurm02hp_foRpgZZ0GY5rFszXf1qzqJpiWFtHc\",\n" +
            "  \"aud\": \"http://mock-data-holder:8081\"\n" +
            "}";

    private static final String JWKS_URL = "http://myurl.com/jwks";

    @Mock
    RemoteJWKSSetHelper remoteJWKSSetHelper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationProperties appProps;

    @Mock
    private RegisteredClientRepository registeredClientRepository;

    @Mock
    private PushAuthorizationRequestRepository parRepo;

    @InjectMocks
    private AccountConsentService acs;

    @Test
    void testGetAccounts() {

        Mockito.when(restTemplate.exchange(
                Mockito.anyString(),
                Mockito.any(HttpMethod.class),
                Mockito.any(HttpEntity.class),
                Mockito.any(Class.class),
                Mockito.anyMap())).thenReturn(new ResponseEntity<>("TEST_DATA", HttpStatus.OK));

        ResponseEntity<String> res = acs.getAccounts("USER1");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("TEST_DATA", res.getBody());
    }

    @Test
    void testAuthoriseWithConsent() {

        Mockito.when(restTemplate.postForEntity(
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(Class.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));


        AccountConsentResponse accountConsent = createTestAccountConsent();
        ResponseEntity<String> res = acs.authoriseWithConsent(
                "cookie", "clientId", "scopes", "state", "acc1,acc2", accountConsent, "cancelConsent");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("", res.getBody());
    }

    @Test
    void testRegisterConsent() {

        AccountConsentResponse accountConsent = createTestAccountConsent();

        Mockito.when(restTemplate.postForObject(
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(Class.class))).thenReturn(accountConsent);

        AccountConsentResponse res = acs.registerConsent(null, "USER1", Arrays.asList("ACCOUNT_ID1", "ACCOUNT_ID1"), 365);
        checkAccountConsent(res);
    }

    //@Test
    void testValidateClientAssertion() throws RemoteKeySourceException {
        List<JWK> jwks = new ArrayList<>();
        JWK adrJwk = Jwks.generateRSAJwk("ADRJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);
        jwks.add(adrJwk);

        String dhAdrClientId = "abcd";
        String signedClientAssertion = generateSignedClientAssertion(adrJwk, dhAdrClientId);

        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);
        when(keySet.get(any(), any())).thenReturn(jwks);

        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);
        RegisteredClient client = Mockito.mock(RegisteredClient.class);
        when(client.getClientSettings()).thenReturn(ClientSettings.builder().jwkSetUrl(JWKS_URL).build());
        when(registeredClientRepository.findByClientId(anyString())).thenReturn(client);
        boolean result = acs.validateClientAssertion(dhAdrClientId, ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER, signedClientAssertion);
        assertTrue(result);
    }

    @Test
    void testValidateClientAssertion_clientNotFound() {
        JWK adrJwk = Jwks.generateRSAJwk("ADRJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);

        String dhAdrClientId = "abcd";
        String signedClientAssertion = generateSignedClientAssertion(adrJwk, dhAdrClientId);

        when(registeredClientRepository.findByClientId(anyString())).thenReturn(null);
        boolean result = acs.validateClientAssertion(dhAdrClientId, ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER, signedClientAssertion);
        assertFalse(result);
    }

    @Test
    void testValidateClientAssertion_InvalidClientAssertion() {
        boolean result = acs.validateClientAssertion("clientId", ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER, "INVALID_CLIENT_ASSERTION");
        assertFalse(result);
    }

    @Test
    void testValidateClientAssertion_InvalidClientAssertionType() {
        boolean result = acs.validateClientAssertion("clientId", "INVALID_CLIENT_ASSERTION_TYPE", "INVALID_CLIENT_ASSERTION");
        assertFalse(result);
    }

    //@Test
    void testValidateClientAssertion_BadSignature() throws RemoteKeySourceException {
        List<JWK> jwks = new ArrayList<>();
        JWK adrJwk = Jwks.generateRSAJwk("ADRJWK-ID", KeyUse.SIGNATURE, JWSAlgorithm.PS256);
        jwks.add(adrJwk);

        JWK adrJwk2 = Jwks.generateRSAJwk("ADRJWK-ID2", KeyUse.SIGNATURE, JWSAlgorithm.PS256);

        String dhAdrClientId = "abcd";
        String signedClientAssertion = generateSignedClientAssertion(adrJwk2, dhAdrClientId);

        RemoteJWKSet<SecurityContext> keySet = Mockito.mock(RemoteJWKSet.class);
        when(keySet.get(any(), any())).thenReturn(jwks);

        when(remoteJWKSSetHelper.getRemoteJWKSet(anyString())).thenReturn(keySet);
        RegisteredClient client = Mockito.mock(RegisteredClient.class);
        when(client.getClientSettings()).thenReturn(ClientSettings.builder().jwkSetUrl(JWKS_URL).build());
        when(registeredClientRepository.findByClientId(anyString())).thenReturn(client);
        boolean result = acs.validateClientAssertion(dhAdrClientId, ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER, signedClientAssertion);
        assertFalse(result);
    }

    @Test
    void testRequestAccountConsent_withRequestUri() {

        String parObj = PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION);

        PushAuthorizationRequestData parData = mock(PushAuthorizationRequestData.class);
        when(parData.getRequestParams()).thenReturn("REQ_PARMS");
        when(parData.getRequestParams()).thenReturn("client_id=xx&response_type=xx&request="+parObj);
        when(parData.isExpired()).thenReturn(false);
        when(parRepo.findByRequestUri(anyString())).thenReturn(parData);
        ModelAndView mv = acs.requestAccountConsent("CLIENT_ID", "SCOPES", "STATE", "REQUEST_URI", null);
        assertNotNull(mv);
    }

    @Test
    void testRequestAccountConsent_withRequestObj() {
        String parObj = PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION);
        ModelAndView mv = acs.requestAccountConsent("CLIENT_ID", "SCOPES", "STATE", null, parObj);
        assertNotNull(mv);
    }

    private String generateSignedClientAssertion(JWK adrJwk, String dhAdrClientId) {
        JSONObject authHeaderJson = new JSONObject(AUTH_PAYLOAD);
        long iat = Instant.now().getEpochSecond();
        long exp = iat + 3600;
        authHeaderJson.put("iat", iat);
        authHeaderJson.put("nbf", iat);
        authHeaderJson.put("exp", exp);
        authHeaderJson.put("iss", dhAdrClientId);
        authHeaderJson.put("sub", dhAdrClientId);
        return Jwks.sign(adrJwk, authHeaderJson.toString());
    }

    private AccountConsentResponse createTestAccountConsent() {
        return new AccountConsentResponse("customerId", "cdrArrangementId", new Timestamp(1));
    }

    private void checkAccountConsent(AccountConsentResponse res) {
        assertNotNull(res);
        assertEquals("customerId", res.getCustomerId());
        assertEquals(1, res.getEndDate().getTime());
    }

}
