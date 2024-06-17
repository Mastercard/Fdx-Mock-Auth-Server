package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.UUID;

@Slf4j
public final class Jwks {

    private Jwks() {
    }
    
    public static JWK loadKeyFromPem(String pem) {
        try {
            JWK key = JWK.parseFromPEMEncodedObjects(pem);
            if (key == null) {
                throw new JOSEException("Failed to load Jwks");
            }
            return key;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static JWK loadRSAJwkFromPem(String keyId, String privKeyPem, String pubKeyPem) throws JOSEException {
        var jwkPub = loadKeyFromPem(pubKeyPem);
        var jwkPriv = loadKeyFromPem(privKeyPem);
        assert jwkPub != null;
        assert jwkPriv != null;
        return new RSAKey.Builder(jwkPub.toRSAKey().toRSAPublicKey())
                .privateKey(jwkPriv.toRSAKey().toRSAPrivateKey())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(keyId)
                .build();
    }
    
    public static JWK generateJwk(KeyUse keyUse, JWSAlgorithm keyAlg, JWK key) throws JOSEException {
        if (key instanceof RSAKey rsaKey)
            return generateRSAJwk(keyUse, keyAlg, rsaKey);
        else
            return generateECJwk(keyUse, keyAlg, (ECKey) key);
    }

    public static JWK generateRSAJwk() {
        return generateRSAJwk(UUID.randomUUID().toString(), KeyUse.SIGNATURE, JWSAlgorithm.PS256);
    }
    public static JWK generateRSAJwk(String keyId, KeyUse keyUse, JWSAlgorithm keyAlg) {

        try {
            // Generate the RSA key pair
            var gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            var keyPair = gen.generateKeyPair();

            // Convert to JWK format
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(keyUse)
                    .algorithm(keyAlg)
                    .keyID(keyId)
                    .build();

        } catch (Exception ex) {
            log.error("Failed to generate RSa JWK - " + ex.getLocalizedMessage());
            return null;
        }
    }
    
    private static RSAKey generateRSAJwk(KeyUse keyUse, JWSAlgorithm keyAlg, RSAKey rsaKey) throws JOSEException {
        var publicKey = rsaKey.toRSAPublicKey();
        var privateKey = rsaKey.toRSAPrivateKey();

        RSAKey rsaJWK = new RSAKey.Builder(publicKey)
                .keyID(rsaKey.getKeyID())
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
    
    private static ECKey generateECJwk(KeyUse keyUse, JWSAlgorithm keyAlg, ECKey ecKey) throws JOSEException {

        ECKey ecJWK = new ECKey.Builder(ecKey)
                .keyID(ecKey.getKeyID())
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

    public static String sign(JWK jwk, String jwtPayload) {
        try {
            var signer = new RSASSASigner(jwk.toRSAKey());

            var jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.PS256).keyID(jwk.toRSAKey().getKeyID()).type(JOSEObjectType.JWT).build(),
                    new Payload(jwtPayload));

            // Compute the RSA signature
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            return null;
        }
    }

    public static boolean verify(JWK jwk, String jwt) {
        try {
            var verifier = new RSASSAVerifier(jwk.toPublicJWK().toRSAKey());
            var jwsObject = JWSObject.parse(jwt);
            return jwsObject.verify(verifier);
        } catch (JOSEException | ParseException e) {
            log.error("Failed to verify signed JWT", e);
            return false;
        }
    }

}
