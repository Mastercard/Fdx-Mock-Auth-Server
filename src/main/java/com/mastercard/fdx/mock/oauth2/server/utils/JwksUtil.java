package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class JwksUtil {

    private JwksUtil() {
    }

    public static JWK generateJwk(KeyUse keyUse, Algorithm keyAlg, JWK key, String kid) throws JOSEException {
        if (key instanceof RSAKey rsaKey)
            return generateRSAJwk(keyUse, keyAlg, rsaKey, kid);
        else
            return generateECJwk(keyUse, keyAlg, (ECKey) key, kid);
    }
    
    private static RSAKey generateRSAJwk(KeyUse keyUse, Algorithm keyAlg, RSAKey rsaKey, String kid) throws JOSEException {
        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
        RSAPrivateKey privateKey = rsaKey.toRSAPrivateKey();

        RSAKey rsaJWK = new RSAKey.Builder(publicKey)
                .keyID(kid)
                .keyUse(keyUse)
                .algorithm(keyAlg)
                .build();

        if (rsaKey.isPrivate()) {
            rsaJWK = new RSAKey.Builder(rsaJWK)
                    .privateKey(privateKey)
                    .build();
        }

        rsaJWK = new RSAKey.Builder(rsaJWK)
                .x509CertChain(rsaKey.getX509CertChain())
                .x509CertSHA256Thumbprint(rsaKey.getX509CertSHA256Thumbprint())
                .build();

        return rsaJWK;
    }
    
    private static ECKey generateECJwk(KeyUse keyUse, Algorithm keyAlg, ECKey ecKey, String kid) throws JOSEException {

        ECKey ecJWK = new ECKey.Builder(ecKey)
                .keyID(kid)
                .keyUse(keyUse)
                .algorithm(keyAlg)
                .build();

        if (ecKey.isPrivate()) {
            ecJWK = new ECKey.Builder(ecJWK)
                    .privateKey(ecKey.toECPrivateKey())
                    .build();
        }

        ecJWK = new ECKey.Builder(ecJWK)
                .x509CertChain(ecJWK.getX509CertChain())
                .x509CertSHA256Thumbprint(ecJWK.getX509CertSHA256Thumbprint())
                .build();

        return ecJWK;
    }

}
