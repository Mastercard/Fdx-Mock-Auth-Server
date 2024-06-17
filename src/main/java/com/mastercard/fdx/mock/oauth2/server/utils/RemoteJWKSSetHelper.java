package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This Helper class allows us to autowired into class that need to obtain a RemoteJWKSet, and during Unit testing
 * can be mocked to return the desired list of JWK's
 */

@Component
@Slf4j
public class RemoteJWKSSetHelper {

    public RemoteJWKSet<SecurityContext> getRemoteJWKSet(String url) {
        try {
            return new RemoteJWKSet<>(new URL(url));
        } catch (MalformedURLException e) {
            log.error("Failed to obtain RemoteJWKSet.", e);
            return null;
        }
    }

}