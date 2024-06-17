package com.mastercard.fdx.mock.oauth2.server.utils;

import com.github.openjson.JSONObject;
import com.nimbusds.jose.jwk.JWK;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class PushAuthorizationRequestTestUtils {

    public static final int DEFAULT_TEST_SHARING_DURATION = 365;

    public static JSONObject generateValidAssertion() {

        Timestamp iat = DateTimeHelper.now();
        Timestamp exp = DateTimeHelper.fromNow(60 * 1000);
        JSONObject assertion = new JSONObject();
        assertion.put("iss", "dataholderClientId");
        assertion.put("sub", "dataholderClientId");
        assertion.put("iat", iat.getTime());
        assertion.put("exp", exp.getTime());
        assertion.put("aud", "dataholderIssuer");
        assertion.put("jti", UUID.randomUUID());

        return assertion;
    }

    public static JSONObject generateValidClaims() {
        return generateClaims(DEFAULT_TEST_SHARING_DURATION);
    }

    public static JSONObject generateClaims(long sharingDuration) {

        JSONObject userInfo = new JSONObject();
        userInfo.put("given_name", null);
        userInfo.put("family_name", null);

        JSONObject acr = new JSONObject();
        acr.put("essential", true);
        acr.put("values", List.of("urn:cds.au:cdr:3"));

        JSONObject idToken = new JSONObject();
        idToken.put("id_token", acr);

        JSONObject claims = new JSONObject();
        if (sharingDuration != -1) {
            claims.put("sharing_duration", sharingDuration);
        }
        claims.put("cdr_arrangement_id", "CDR_ARRANGEMENT_ID");
        claims.put("userinfo", userInfo);
        claims.put("id_token", idToken);

        return claims;

    }

    public static JSONObject generateValidPAR() {
        return generatePAR(true, DEFAULT_TEST_SHARING_DURATION);
    }

    public static JSONObject generatePAR(boolean includeClaims, long sharingDuration) {

        Timestamp iat = DateTimeHelper.now();
        Timestamp exp = DateTimeHelper.fromNow(60 * 1000);

        JSONObject par = new JSONObject();
        par.put("iss", "dataholderClientId");
        par.put("sub", "user1a");
        par.put("iat", iat.getTime());
        par.put("exp", exp.getTime());
        par.put("aud", "dataholderIssuer");
        par.put("jti", UUID.randomUUID());
        par.put("response_type", "code id_token");
        par.put("client_id", "dataholderClientId");
        par.put("redirect_uri", "adrRedirectUrl");
        par.put("response_mode", "oauth2ResponseMode");
        par.put("scope", "openid profile bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read common:customer.basic:read");
        par.put("state", UUID.randomUUID());
        par.put("nonce", UUID.randomUUID());
        if (includeClaims)
            par.put("claims", generateClaims(sharingDuration));

        return par;
    }

    public static String generateValidPARPostData(String assertionJwt, String parJwt) {
        return "grant_type=client_credentials" +
                "&client_id=dataholderClientId" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_assertion=" + assertionJwt +
                "&scope=openid profile bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read common:customer.basic:read" +
                "&request=" + parJwt +
                "&response_type=code id_token";

    }

    public static String generateSignedPAR(JWK jwk, boolean includeClaims, long sharingDuration) {

        JSONObject par = generatePAR(includeClaims, sharingDuration);
        String parJwt = Jwks.sign(jwk, par.toString());

        return parJwt;
    }

    public static String generateValidPARPostData() {
        return generatePARPostData(true, DEFAULT_TEST_SHARING_DURATION);
    }

    public static String generatePARPostData(boolean includeClaims, long sharingDuration) {

        JWK jwk = Jwks.generateRSAJwk();

        String parJwt = generateSignedPAR(jwk, includeClaims, sharingDuration);

        JSONObject assertion = generateValidAssertion();
        String assertionJwt = Jwks.sign(jwk, assertion.toString());

        return generateValidPARPostData(assertionJwt, parJwt);

    }

}
