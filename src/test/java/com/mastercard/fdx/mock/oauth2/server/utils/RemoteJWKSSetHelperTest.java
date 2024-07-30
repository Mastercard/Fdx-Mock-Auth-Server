package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemoteJWKSSetHelperTest {

    public static final String VALID_JWKS_URL = "https://fin03.dev.openbanking.mastercard.com.au/jwks";
    public static final String INVALID_JWKS_URL = VALID_JWKS_URL + "_invalid";
    public static final String MALFORMED_URL = "MALFORMED_URL";

    @Test
    void testValidUrl() {
        RemoteJWKSSetHelper remoteJWKSSetHelper = new RemoteJWKSSetHelper();
        JWKSource<SecurityContext> remoteJWKSet = remoteJWKSSetHelper.getRemoteJWKSet(VALID_JWKS_URL);
        Assertions.assertNotNull(remoteJWKSet);
    }

    @Test
    void testInvalidUrl() {
        RemoteJWKSSetHelper remoteJWKSSetHelper = new RemoteJWKSSetHelper();
        JWKSource<SecurityContext>  remoteJWKSet = remoteJWKSSetHelper.getRemoteJWKSet(INVALID_JWKS_URL);
        Assertions.assertNotNull(remoteJWKSet);
    }

    @Test
    void testMalformedUrl() {
        RemoteJWKSSetHelper remoteJWKSSetHelper = new RemoteJWKSSetHelper();
        JWKSource<SecurityContext>  remoteJWKSet = remoteJWKSSetHelper.getRemoteJWKSet(MALFORMED_URL);
        Assertions.assertNull(remoteJWKSet);
    }

}
