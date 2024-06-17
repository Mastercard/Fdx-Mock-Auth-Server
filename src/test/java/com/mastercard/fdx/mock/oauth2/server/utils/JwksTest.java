package com.mastercard.fdx.mock.oauth2.server.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwksTest {

	private final String RSA_EXAMPLE_JWKS_PUBLIC_PEM = "-----BEGIN CERTIFICATE-----\n" +
			"MIIEOzCCAyOgAwIBAgIUZLNJouIlvHFK2yiXpIoSBoRgWcEwDQYJKoZIhvcNAQEL\n" +
			"BQAwZTEYMBYGA1UEAwwPTW9ja1JlZ2lzdGVyU1NBMQswCQYDVQQGEwJBVTEMMAoG\n" +
			"A1UECAwDQUNUMREwDwYDVQQHDAhDYW5iZXJyYTENMAsGA1UECgwEQUNDQzEMMAoG\n" +
			"A1UECwwDQ0RSMB4XDTIyMDUwNDAzMDkwNVoXDTIzMDYwODAzMDkwNVowZTEYMBYG\n" +
			"A1UEAwwPTW9ja1JlZ2lzdGVyU1NBMQswCQYDVQQGEwJBVTEMMAoGA1UECAwDQUNU\n" +
			"MREwDwYDVQQHDAhDYW5iZXJyYTENMAsGA1UECgwEQUNDQzEMMAoGA1UECwwDQ0RS\n" +
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxcauZjCyzGNN39Gx97l9\n" +
			"e1dJg2ka1odvavyq/nw514DJpvLgoqV+O+nPZ4J1qNk3u9xgy+KS0UEJDTRUudbB\n" +
			"bNR25433X9zF78QDbgBjFpAwwvpt+lU3VcGTdOcS1wqalXWL//ylIze6YCkugPRG\n" +
			"E85bAphH985xDl9430CO7R4bIibkQV0QtJIwp/naNKNeGOrf0+H0Wo+VtdxzsjSp\n" +
			"AeZXi7WfOYrV88jasZWzrykjd9z5IF4yjIj+xQ4uUyH8FI1xaprbSNKASfsTOGW7\n" +
			"DN3/uliNrRpDEr+1kRrTyEvoh/l+UOtRlGMAVv+ayV/0F+QFIhtKN8A4FYjE6gCT\n" +
			"4QIDAQABo4HiMIHfMIGMBgNVHSMEgYQwgYGhaaRnMGUxGDAWBgNVBAMMD01vY2tS\n" +
			"ZWdpc3RlclNTQTELMAkGA1UEBhMCQVUxDDAKBgNVBAgMA0FDVDERMA8GA1UEBwwI\n" +
			"Q2FuYmVycmExDTALBgNVBAoMBEFDQ0MxDDAKBgNVBAsMA0NEUoIUZLNJouIlvHFK\n" +
			"2yiXpIoSBoRgWcEwCQYDVR0TBAIwADAOBgNVHQ8BAf8EBAMCBeAwFgYDVR0lAQH/\n" +
			"BAwwCgYIKwYBBQUHAwEwGwYDVR0RBBQwEoIQbW9ja3JlZ2lzdGVyLnNzYTANBgkq\n" +
			"hkiG9w0BAQsFAAOCAQEAreKTXlr59RuchusB86+P8zidNSWPq3aY+aAB0+UAGhkC\n" +
			"DNFguxX+tGscWnG7ds0yY4BdJ0yNo+EQ0i9uKv9RLQWmKsB7KV7F+SB3UwgPYjUl\n" +
			"oKdv3gV2fXrWu/lAXmM+IR9AJIOLYzblwVK0ukvcmvdcLzTBa54JEuxS2OENTgDt\n" +
			"XKiO19VJ0lg6ROUJ5fn6oLM6czJ0IcleC8UN16JaHSFuTNHPtzocE60X9320Dn5h\n" +
			"7ut9GBidkZ/JgicGSjImlTe/9aJ4D8jjRSpAPxdV9wtHndZ/4oJbr6t4aLLZD3KF\n" +
			"sO3kIi326sobDUb7oP9EF4egdWUXSN1MI9kM83os8A==\n" +
			"-----END CERTIFICATE-----\n";

	public static final String RSA_EXAMPLE_JWKS_PRIVATE_PEM = "-----BEGIN PRIVATE KEY-----\n" +
			"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFxq5mMLLMY03f\n" +
			"0bH3uX17V0mDaRrWh29q/Kr+fDnXgMmm8uCipX476c9ngnWo2Te73GDL4pLRQQkN\n" +
			"NFS51sFs1Hbnjfdf3MXvxANuAGMWkDDC+m36VTdVwZN05xLXCpqVdYv//KUjN7pg\n" +
			"KS6A9EYTzlsCmEf3znEOX3jfQI7tHhsiJuRBXRC0kjCn+do0o14Y6t/T4fRaj5W1\n" +
			"3HOyNKkB5leLtZ85itXzyNqxlbOvKSN33PkgXjKMiP7FDi5TIfwUjXFqmttI0oBJ\n" +
			"+xM4ZbsM3f+6WI2tGkMSv7WRGtPIS+iH+X5Q61GUYwBW/5rJX/QX5AUiG0o3wDgV\n" +
			"iMTqAJPhAgMBAAECggEAb69X8utTPOpjIp6XQo5kFw/euq/S8QzAVYljwpxCSbk3\n" +
			"puiHBDIyjADoF8rrSLOJrrOvkdNcF4EDbIp9wghNaHi+wNpBtMfGmzR0v1zGmApL\n" +
			"DwA+tNwEiI/tBlorIHajfv1HVwLD+pRQ0lbPJRFMtYiVimyWT5Er9PLjHielzq5v\n" +
			"aeUk5gqa5iJig8vRZYhsd4S01w24Jnc42xfH0PeFCAiEeo42E5mQCienIygOsZdn\n" +
			"DESzBR1qP49KfP8FXZoMdoQCBrsF9pHUwY6YhyCDQf1YqKMlpMyPtk4BQC6pVSi7\n" +
			"Ll6sm9AjGbxH5UhCSStJiWqsG5EKQeYnfCbqKBiEsQKBgQD2yTq9MaliHe++G80+\n" +
			"9rvd2LCdcQ0gGWYW16cZnjmdF+lhxwD8tnp3FhLD4UV5zCi4xmzVLh8OOVZ8lQg9\n" +
			"yV0OcPjBtp03sssS46NKRHjMowYZHKAVkcyhKaElaZ24ww+kQlj722jtmSPEU21a\n" +
			"QG96oBfWhOC3HgdFg7fA0AvdJQKBgQDNKQRJyp901pUbRYMq2R8UutIQAKh3rEej\n" +
			"f9+npIxu2rYIuJpDDi+XLmVj/2B9VQuh0aAtuoEqFlQspaW0WpkGLWyap98Wcxdq\n" +
			"8QRpMw8QsRm91QoALQ0skxBixqCS1cvr51GCDCsjGwWr9yyusF7cnWCi3RSu5rnJ\n" +
			"7cAtZbAlDQKBgQCwGSWmDYLttaj2gkYyMMQb6N0Wa65PAnC7ygS+Hg7Ej2SE1glZ\n" +
			"SykWQxT+24fFAq7oMwdEE5YrgzS4z2vDEyYpAOow6ItFTHrmLrO9ciC0/XuDfc6p\n" +
			"tZFMSzVnSfc2+VH3BzUR+x3jtYw1MsbsSOCDoyt/cvAbGv1bWENN/ecJVQKBgQCF\n" +
			"or8RVfPSBmhvsDLFeVJejWaBV/xkn4q2fHfx2iRDnSFPwvdw0E+9mVIf9dwjJ3+B\n" +
			"M2uz/bVe9dZ0kp3CQfVFaddBUjHljKhnV4E5CTI5/DI5TgjKno3jSBQ3UwyLOMow\n" +
			"6qdUH5sFrfde0Y2AaJASAk1NKNbtKotAdiZlK8S2DQKBgGdD5NSzlxcWorQ1AvGP\n" +
			"ce5C/vyDg5zioRwMkA+dpKQwMff0LUg7xKHokiA2TP+QF+1Gyy4VCzZEt+9R5mGp\n" +
			"P5SUi0UMXSCXECvtcVbxrs0R5bwoy+vOwsH3/gL5JUMfM0/R5vM/zQ0SiKEkLMn3\n" +
			"94+bD9F3U3Bqk2DjZtgYvttA\n" +
			"-----END PRIVATE KEY-----\n";

	public static final String EC_EXAMPLE_JWKS_PRIVATE_PEM = "-----BEGIN PRIVATE KEY-----\n" +
            "MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDD9IP9Xf39mSvm0wTEE\n" +
            "l7jqfNMoM78tKpmFKmpvt0SffYbaCBlZUzc2dzHEtfWGppKhZANiAATgEeOCRte6\n" +
            "EfZ649b40xNNiOl5WXhFuO40jMo2amD5ZEpnbIyIqgNdsR8temy+20szGo66M8x+\n" +
            "CJT4DZJefNjQsCLl8n3KveViyhxeRP0QMbmJ1en+DsNLo4yzGdqDprM=\n" +
            "-----END PRIVATE KEY-----\n";


	@Test
    void testGenerateRSAJwk_PrivatePem() throws JOSEException {
        	JWK jwk = Jwks.loadKeyFromPem(RSA_EXAMPLE_JWKS_PRIVATE_PEM);
        	assertNotNull(jwk);

        	JWK jwk2 = Jwks.generateJwk(KeyUse.SIGNATURE, JWSAlgorithm.PS256, jwk);
        	assertNotNull(jwk2);
        	assertNull(jwk2.getKeyID());
        	assertEquals(KeyType.RSA, jwk2.getKeyType());
        	assertEquals(JWSAlgorithm.PS256, jwk2.getAlgorithm());
        	assertEquals(KeyUse.SIGNATURE, jwk2.getKeyUse());
        	assertTrue(jwk2.isPrivate());
    }

	@Test
	void testGenerateRSAJwk_PublicPem() throws JOSEException {
		JWK jwk = Jwks.loadKeyFromPem(RSA_EXAMPLE_JWKS_PUBLIC_PEM);
		assertNotNull(jwk);

		JWK jwk2 = Jwks.generateJwk(KeyUse.SIGNATURE, JWSAlgorithm.PS256, jwk);
		assertNotNull(jwk2);
		assertEquals(KeyType.RSA, jwk2.getKeyType());
		assertEquals(JWSAlgorithm.PS256, jwk2.getAlgorithm());
		assertEquals(KeyUse.SIGNATURE, jwk2.getKeyUse());
		assertFalse(jwk2.isPrivate());
	}

	 @Test
	 void testGenerateECJwk_PrivatePem_NotAllowed() {
		 	JWK jwk = Jwks.loadKeyFromPem(EC_EXAMPLE_JWKS_PRIVATE_PEM);
		 	assertNull(jwk);
	 }

	@Test
	void testLoadJWKFromPem() throws JOSEException {
		String keyId = "SPECIFIC_KEY_ID";
		JWK jwk = Jwks.loadRSAJwkFromPem(keyId, RSA_EXAMPLE_JWKS_PRIVATE_PEM, RSA_EXAMPLE_JWKS_PUBLIC_PEM);
		assertTrue(jwk.isPrivate());
		assertNotNull(jwk.toRSAKey().toPrivateKey());
		assertNotNull(jwk.toPublicJWK());
	}

	@Test
	void testValidSignAndVerify() {
		JWK jwk = Jwks.generateRSAJwk();
		String signedPayload = Jwks.sign(jwk, "payload");
		assertTrue(Jwks.verify(jwk, signedPayload));
	}

	@Test
	void testValidSignAndVerifyWithKeyId() {
		String keyId = "SPECIFIC_KEY_ID";
		JWK jwk = Jwks.generateRSAJwk(keyId, KeyUse.SIGNATURE, JWSAlgorithm.PS256);
		assertEquals(keyId, jwk.getKeyID());

		String signedPayload = Jwks.sign(jwk, "payload");
		assertTrue(Jwks.verify(jwk, signedPayload));
	}

}
