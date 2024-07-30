package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicClientRegistrationServiceTest {

    private final String validOriginalDcrApiResponse = """
            {
                "client_id": "xv4ZdeIN4QyLbpGtjFi8nMsnT2xK2MFCCX1gx0T8XqU",
                "client_id_issued_at": 1716785330,
                "client_name": "11TestName1221",
                "redirect_uris": [
                    "https://mybankAuthServer.com/callback"
                ],
                "grant_types": [
                    "refresh_token",
                    "client_credentials",
                    "authorization_code"
                ],
                "response_types": [
                    "code"
                ],
                "scope": "client.create client.read",
                "token_endpoint_auth_method": "private_key_jwt",
                "id_token_signed_response_alg": "RS256",
                "registration_client_uri": "http://127.0.0.1:8080/client/register?client_id=xv4ZdeIN4QyLbpGtjFi8nMsnT2xK2MFCCX1gx0T8XqU",
                "jwks_uri": "https://www.jsonkeeper.com/b/3FJT",
                "token_endpoint_auth_signing_alg": "PS256",
                "registration_access_token": "eyJraWQiOiI5OWJjMzNlZS1hMDRkLTRhODktOGFlMC01ZGViZDIxNDVhYWIiLCJ0eXAiOiJhdCtqd3QiLCJhbGciOiJQUzI1NiJ9.eyJzdWIiOiJ4djRaZGVJTjRReUxicEd0akZpOG5Nc25UMnhLMk1GQ0NYMWd4MFQ4WHFVIiwiYXVkIjoieHY0WmRlSU40UXlMYnBHdGpGaThuTXNuVDJ4SzJNRkNDWDFneDBUOFhxVSIsIm5iZiI6MTcxNjc4NTMzMCwic2NvcGUiOlsiY2xpZW50LnJlYWQiXSwiaXNzIjoiaHR0cDovLzEyNy4wLjAuMTo4MDgwIiwiZXhwIjoxNzE2Nzg1NjMwLCJpYXQiOjE3MTY3ODUzMzAsImp0aSI6ImZjZjdhNjMxLWQ3ZjQtNDAzOC05NTI5LWQwMTMyZWIzOWMzYiJ9.bK9qkSvwdrGxT4Qtop0onrMx9_E-U8th-AnapjRRwW7ivOFAfdttGxUJMHXnxbnSXKWgOCjdbUsrjohgr5bOkQaTf6qfoFhDU_19gU09qe9HctIYKiNFJXP3FOpXfXVRdddq9oRKn_eRtLYLJ5FbLTr2OrFTNXDrPnl-2m7Y8-uS4c7KxVMstdLQJ8eE3-9Wg7ieF1uj5Rz_Zf-VdMURqIKhSwXIMlHW3jrkIDSpxFyrCjymkq9InZv6N5WluQK5f7_8Tpe37F8YAwB4vdAtVAKEt9tNxN5ylphaxtU7_sfdqYTDTghGkzJLOOMyWDIX0zrWAlBWfsRw5OZvshQMtQ"
            }
            """;
    @Mock
    RemoteJWKSSetHelper remoteJWKSSetHelper;
    @Mock
    AuthServerService authServerService;
    @Mock
    ApplicationProperties appProps;
    @Mock
    ClientDeletionService clientDeletionService;
    @Mock
    AuthorizationValidatorService authorizationValidatorService;
    @Mock
    private RegisteredClientRepository registeredClientRepository;

    @InjectMocks
    DynamicClientRegistrationService dcrService;

    @Test
    void testInvalidDcrJwtRequest() {
        ErrorResponse ex = assertThrows(ErrorResponse.class, () -> dcrService.register("{\"client_name:\"ClientNameInvalidJson\"}"));
        assertEquals(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA, ex.getError());
        assertEquals("Expected ':' after client_name: at character 16 of {\"client_name:\"ClientNameInvalidJson\"}", ex.getErrorDescription());
    }

    @Test
    void testValidDcrJwtRequest() throws ErrorResponse, JOSEException {

        String createClientRequestPayload = """
                {
                "client_name": "11TestName1221",
                "redirect_uris": ["https://mybankAuthServer.com/callback"],
                "jwks_uri": "https://www.jsonkeeper.com/b/3FJT",
                "scope": "client.create client.read",
                "token_endpoint_auth_method": "private_key_jwt",
                "token_endpoint_auth_signing_alg": "PS256",
                "grant_types": [
                    "client_credentials",
                    "authorization_code",
                    "refresh_token"
                ],
                "response_types": [
                    "code"
                ],
                "id_token_signed_response_alg": "PS256",
                "id_token_encrypted_response_alg": "RSA-OAEP",
                "id_token_encrypted_response_enc": "A256GCM",
                "request_object_signing_alg": "PS256"
                }
                """;

        when(authServerService.getAccessToken(any(), any())).thenReturn("DUMMY_ACCESS_TOKEN");
        when(authServerService.registerClient(any(), anyString())).thenReturn(new ResponseEntity<>(validOriginalDcrApiResponse, HttpStatus.CREATED));

        ResponseEntity<String> resp = dcrService.register(createClientRequestPayload);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        JSONObject respJson = new JSONObject(resp.getBody());
        assertEquals("xv4ZdeIN4QyLbpGtjFi8nMsnT2xK2MFCCX1gx0T8XqU", respJson.getString("client_id"));
        assertEquals("11TestName1221", respJson.getString("client_name"));
        assertEquals("client.create client.read", respJson.getString("scope"));
    }

    @Test
    void testGetClient() throws ErrorResponse {
        dcrService.get("a", "b");
        verify(authServerService).getClient("a", "b");
    }

    @Test
    void testDeleteClient() throws ErrorResponse {
        when(clientDeletionService.deleteClient("a")).thenReturn("a");
        dcrService.delete("a", "qrst");
        verify(clientDeletionService).deleteClient("a");
    }

    @Test
    void testInValidDCMJwtRequest() throws ErrorResponse, JOSEException {

        String createClientRequestPayload = """
                {
                "client_name": "11TestName1221",
                "redirect_uris": ["https://mybankAuthServer.com/callback"],
                "jwks_uri": "https://www.jsonkeeper.com/b/3FJT",
                "scope": "client.create client.read",
                "token_endpoint_auth_method": "private_key_jwt",
                "token_endpoint_auth_signing_alg": "PS256",
                "grant_types": [
                    "client_credentials",
                    "authorization_code",
                    "refresh_token"
                ],
                "response_types": [
                    "code"
                ],
                "id_token_signed_response_alg": "PS256",
                "id_token_encrypted_response_alg": "RSA-OAEP",
                "id_token_encrypted_response_enc": "A256GCM",
                "request_object_signing_alg": "PS256"
                }
                """;

        ErrorResponse ex = assertThrows(ErrorResponse.class, () ->  dcrService.modify(createClientRequestPayload, "", ""));
        assertEquals(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA, ex.getError());
    }

    @Test
    void testInValidDCM() throws ErrorResponse, JOSEException, BadJOSEException, ParseException,
			com.nimbusds.oauth2.sdk.ParseException {
        doThrow(ParseException.class).when(authorizationValidatorService).validate(anyString(), anyString());
        ErrorResponse ex = assertThrows(ErrorResponse.class, () ->  dcrService.modify("createClientRequestPayload", "", ""));
        assertEquals(DynamicClientRegistrationService.ERROR_UNAUTHORIZED, ex.getError());
    }
}
