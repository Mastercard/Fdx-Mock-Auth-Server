package com.mastercard.fdx.mock.oauth2.server.service;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
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

@Service
@Slf4j
public class AuthorizationValidatorService {
    @Autowired
    private RemoteJWKSSetHelper remoteJWKSSetHelper;

    @Autowired
    private ApplicationProperties appProps;

    /**
     * Below method validates the jwt authorization token from DCR flow.
     * @param authorisation
     * @param clientId
     * @throws ParseException
     * @throws BadJOSEException
     * @throws JOSEException
     * @throws com.nimbusds.oauth2.sdk.ParseException
     */
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

    /**
     * Below method validates the jwt authorization token for other APIs like Consent, token.
     * @param authorisation
     * @throws BadJOSEException
     * @throws ParseException
     * @throws JOSEException
     * @throws com.nimbusds.oauth2.sdk.ParseException
     */
    public void validateAccessToken(String authorisation)
            throws BadJOSEException, ParseException, JOSEException {

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
        JWKSource<SecurityContext> keySource = remoteJWKSSetHelper.getRemoteJWKSet(appProps.getAuthServerBaseUrl() + appProps.getAuthServerJwksUriPath());
        JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.PS256, keySource);

        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, new JOSEObjectType("at+jwt")));
        jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.process(authToken, null);
    }
}