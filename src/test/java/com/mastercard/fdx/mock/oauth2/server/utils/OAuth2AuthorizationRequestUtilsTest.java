package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2AuthorizationRequestUtilsTest {

    private final JWK jwk = Jwks.generateRSAJwk();

    @Test
    void testAdjustSharingDuration_NegativeValues() {
        long sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(-1);
        assertEquals(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION, sd);
    }

    @Test
    void testAdjustSharingDuration_OneTimeConsent() {
        long sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(0);
        assertEquals(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION, sd);
    }

    @Test
    void testAdjustSharingDuration_PositiveValue_LessThan_OneTimeConsent() {
        long sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION - 1);
        assertEquals(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION, sd);

        sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION);
        assertEquals(OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION, sd);
    }

    @Test
    void testAdjustSharingDuration_MoreThan_OneTimeConsent() {
        long exp = OAuth2AuthorizationRequestUtils.ONE_TIME_CONTENT_SHARING_DURATION + 1;
        long sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(exp);
        assertEquals(exp, sd);
    }

    @Test
    void testAdjustSharingDuration_MoreThan_MaxSharingDuration() {
        long sd = OAuth2AuthorizationRequestUtils.adjustSharingDuration(OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION + 1);
        assertEquals(OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION, sd);
    }

    @Test
    void testGetSharingDuration_NoRequest() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        assertEquals(-1, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_InvalidRequest() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", "INVALID_PAR_OBJECT");
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        assertEquals(-1, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_ValidRequest() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertEquals(OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_ValidRequest_MissingClaims() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, false, OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertEquals(-1, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_ValidRequest_MissingSharingDuration() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, -1));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertEquals(-1, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_ValidRequest_OneTimeConsent_SharingDuration() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 0));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertEquals(0, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testGetSharingDuration_ValidRequest_Positive_SharingDuration() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 100));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertEquals(100, OAuth2AuthorizationRequestUtils.getSharingDuration(authReq));
    }

    @Test
    void testIsOneTimeConsentRequest_MissingRequest() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertFalse(OAuth2AuthorizationRequestUtils.isOneTimeConsentRequest(authReq));
    }

    @Test
    void testIsOneTimeConsentRequest_ValidRequest_OneTimeConsent() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 0));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertTrue(OAuth2AuthorizationRequestUtils.isOneTimeConsentRequest(authReq));
    }

    @Test
    void testIsOneTimeConsentRequest_ValidRequest_NonOneTimeConsent() {
        OAuth2AuthorizationRequest authReq = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> addParams = new HashMap<>();
        addParams.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 1));
        when(authReq.getAdditionalParameters()).thenReturn(addParams);
        when(authReq.getClientId()).thenReturn("user1");
        assertFalse(OAuth2AuthorizationRequestUtils.isOneTimeConsentRequest(authReq));
    }

    @Test
    void testGetOAuth2AuthorizationRequest_MissingAuthorization() {
        OAuth2TokenContext context = mock(OAuth2TokenContext.class);
        assertNull(OAuth2AuthorizationRequestUtils.getOAuth2AuthorizationRequest(context));
    }

    @Test
    void testGetOAuth2AuthorizationRequest_HasAuthorization_MissingOAuth2AuthorizationRequest() {
        OAuth2TokenContext context = mock(OAuth2TokenContext.class);
        OAuth2Authorization auth = mock(OAuth2Authorization.class);
        Map<String, Object> attr = new HashMap<>();
        attr.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 1));
        when(context.getAuthorization()).thenReturn(auth);
        when(auth.getAttributes()).thenReturn(attr);
        assertNull(OAuth2AuthorizationRequestUtils.getOAuth2AuthorizationRequest(context));
    }

    @Test
    void testGetOAuth2AuthorizationRequest_HasAuthorization_HasOAuth2AuthorizationRequest() {
        OAuth2TokenContext context = mock(OAuth2TokenContext.class);
        OAuth2Authorization auth = mock(OAuth2Authorization.class);
        OAuth2AuthorizationRequest authRequest = mock(OAuth2AuthorizationRequest.class);
        Map<String, Object> attr = new HashMap<>();
        attr.put(OAuth2AuthorizationRequest.class.getName(), authRequest);
        attr.put("request", PushAuthorizationRequestTestUtils.generateSignedPAR(jwk, true, 1));
        when(context.getAuthorization()).thenReturn(auth);
        when(auth.getAttributes()).thenReturn(attr);

        OAuth2AuthorizationRequest res = OAuth2AuthorizationRequestUtils.getOAuth2AuthorizationRequest(context);
        assertNotNull(res);
    }

}
