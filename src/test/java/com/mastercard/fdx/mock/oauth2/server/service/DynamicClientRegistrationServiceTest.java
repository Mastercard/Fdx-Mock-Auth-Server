package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ClientConstant;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.entity.OAuth2RegisteredClientFDX;
import com.mastercard.fdx.mock.oauth2.server.repository.OAuth2RegisteredClientRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock
    private OAuth2RegisteredClientRepository oAuth2RegisteredClientRepository;
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
                "request_object_signing_alg": "PS256",
                "client_uri": "https://example.net/",
                    "contacts": [
                        "support@example.net"
                    ],
                    "description": "Recipient Application servicing financial use case requiring permissioned data sharing",
                    "duration_period": 365,
                    "duration_type": [
                        "TIME_BOUND"
                    ],
                    "intermediaries": [
                        {
                            "contacts": [
                                "support@partner.com"
                            ],
                            "description": "Data Access Platform specializing in servicing permissioned data sharing for Data Recipients",
                            "logo_uri": "https://partner.example/logo.png",
                            "name": "Data Access Platform Name",
                            "registry_references": [
                                {
                                    "registered_entity_id": "JJH7776512TGMEJSG",
                                    "registered_entity_name": "Data Access Platform listed company Name",
                                    "registry": "FDX"
                                }
                            ],
                            "uri": "https://partner.example/"
                        },
                        {
                            "contacts": [
                                "support@sub-partner-one.com"
                            ],
                            "description": "Digital Service Provider to the Recipient",
                            "logo_uri": "https://sub-partner-one.example/logo.png",
                            "name": "Digital Service Provider Name",
                            "registry_references": [
                                {
                                    "registered_entity_id": "9LUQNDG778LI9D1",
                                    "registered_entity_name": "Service Provider listed company Name",
                                    "registry": "GLEIF"
                                }
                            ],
                            "uri": "https://sub-partner-one.example/"
                        }
                    ],
                    "logo_uri": "https://client.example.org/logo.png",
                    "lookback_period": 365,
                    "registry_references": [
                        {
                            "registered_entity_id": "4HCHXIURY78NNH6JH",
                            "registered_entity_name": "Official recipient name",
                            "registry": "GLEIF"
                        }
                    ]
                }
                """;

        when(authServerService.getAccessToken(any(), any())).thenReturn("DUMMY_ACCESS_TOKEN");
        when(authServerService.registerClient(any(), anyString())).thenReturn(new ResponseEntity<>(validOriginalDcrApiResponse, HttpStatus.CREATED));

        // Create a mock client object to be returned by the repository
        OAuth2RegisteredClientFDX mockClient = new OAuth2RegisteredClientFDX();
        mockClient.setId("xv4ZdeIN4QyLbpGtjFi8nMsnT2xK2MFCCX1gx0T8XqU");
        mockClient.setClientUri("https://example.net/");
        mockClient.setContacts("[\"support@example.net\"]");
        mockClient.setDescription("Recipient Application servicing financial use case requiring permissioned data sharing");
        mockClient.setDurationType("[\"TIME_BOUND\"]");
        mockClient.setDurationPeriod("365");
        mockClient.setLookbackPeriod("365");
        mockClient.setLogoUri("https://client.example.org/logo.png");
        mockClient.setRegistryReferences("[{\"registered_entity_id\":\"4HCHXIURY78NNH6JH\",\"registered_entity_name\":\"Official recipient name\",\"registry\":\"GLEIF\"}]");
        mockClient.setIntermediaries("[{\"name\":\"Data Access Platform Name\"},{\"name\":\"Digital Service Provider Name\"}]");

        // Mock the repository's save method to return our mock client
        when(oAuth2RegisteredClientRepository.save(any(OAuth2RegisteredClientFDX.class))).thenReturn(mockClient);

        ResponseEntity<String> resp = dcrService.register(createClientRequestPayload);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        JSONObject respJson = new JSONObject(resp.getBody());
        assertEquals("xv4ZdeIN4QyLbpGtjFi8nMsnT2xK2MFCCX1gx0T8XqU", respJson.getString("client_id"));
        assertEquals("11TestName1221", respJson.getString("client_name"));
        assertEquals("client.create client.read", respJson.getString("scope"));
    }

    @Test
    void testGetClient_Success() throws ErrorResponse {
        // Arrange
        String clientId = "test-client-id";
        String dhDcrAccessToken = "test-access-token";

        // Mock the response from authServerService.getClient
        String clientResponseBody = """
        {
            "client_id": "test-client-id",
            "client_name": "Test Client",
            "scope": "client.create client.read"
        }
    """;
        ResponseEntity<String> mockResponse = new ResponseEntity<>(clientResponseBody, HttpStatus.OK);
        when(authServerService.getClient(clientId, dhDcrAccessToken)).thenReturn(mockResponse);

        // Mock the repository to return a valid client
        OAuth2RegisteredClientFDX mockClient = new OAuth2RegisteredClientFDX();
        mockClient.setClientUri("https://example.com");
        mockClient.setContacts("[\"support@example.com\"]");
        mockClient.setDescription("Test Description");
        mockClient.setDurationType("[\"TIME_BOUND\"]");
        mockClient.setDurationPeriod("365");
        mockClient.setLookbackPeriod("365");
        mockClient.setLogoUri("https://example.com/logo.png");
        mockClient.setRegistryReferences("[{\"registered_entity_id\":\"12345\",\"registry\":\"GLEIF\"}]");
        mockClient.setIntermediaries("[{\"name\":\"Intermediary Name\"}]");
        when(oAuth2RegisteredClientRepository.findById(clientId)).thenReturn(Optional.of(mockClient));

        // Act
        ResponseEntity<String> response = dcrService.get(clientId, dhDcrAccessToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONObject responseBody = new JSONObject(response.getBody());
        assertEquals("test-client-id", responseBody.getString("client_id"));
        assertEquals("Test Client", responseBody.getString("client_name"));
        assertEquals("client.create client.read", responseBody.getString("scope"));
        assertEquals("https://example.com", responseBody.getString(ClientConstant.CLIENT_URI));
        assertEquals("[\"support@example.com\"]", responseBody.getJSONArray(ClientConstant.CONTACTS).toString());
        assertEquals("Test Description", responseBody.getString(ClientConstant.DESCRIPTION));
        assertEquals("[\"TIME_BOUND\"]", responseBody.getJSONArray(ClientConstant.DURATION_TYPE).toString());
        assertEquals("365", responseBody.getString(ClientConstant.DURATION_PERIOD));
        assertEquals("365", responseBody.getString(ClientConstant.LOOKBACK_PERIOD));
        assertEquals("https://example.com/logo.png", responseBody.getString(ClientConstant.LOGO_URI));
        assertEquals("[{\"registered_entity_id\":\"12345\",\"registry\":\"GLEIF\"}]", responseBody.getJSONArray(ClientConstant.REGISTRY_REFERENCES).toString());
        assertEquals("[{\"name\":\"Intermediary Name\"}]", responseBody.getJSONArray(ClientConstant.INTERMEDIARIES).toString());
    }

    @Test
    void testGetClient_ClientNotFound() throws ErrorResponse {
        // Arrange
        String clientId = "non-existent-client-id";
        String dhDcrAccessToken = "test-access-token";

        // Mock the response from authServerService.getClient
        String clientResponseBody = """
        {
            "client_id": "non-existent-client-id"
        }
    """;
        ResponseEntity<String> mockResponse = new ResponseEntity<>(clientResponseBody, HttpStatus.OK);
        when(authServerService.getClient(clientId, dhDcrAccessToken)).thenReturn(mockResponse);

        // Mock the repository to return empty
        when(oAuth2RegisteredClientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        ErrorResponse ex = assertThrows(ErrorResponse.class, () -> dcrService.get(clientId, dhDcrAccessToken));
        assertEquals(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA, ex.getError());
    }

    @Test
    void testGetClient_AuthServerError() throws ErrorResponse {
        // Arrange
        String clientId = "test-client-id";
        String dhDcrAccessToken = "test-access-token";

        // Mock the response from authServerService.getClient to return an error
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        when(authServerService.getClient(clientId, dhDcrAccessToken)).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> response = dcrService.get(clientId, dhDcrAccessToken);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error occurred", response.getBody());
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


    @Test
    void testSaveClientRegistration_Success() {
        // Arrange
        String clientId = "test-client-id";
        JSONObject clientRegistrationJson = new JSONObject();
        clientRegistrationJson.put(ClientConstant.CLIENT_URI, "https://example.com");
        clientRegistrationJson.put(ClientConstant.CONTACTS, "[\"support@example.com\"]");
        clientRegistrationJson.put(ClientConstant.DESCRIPTION, "Test Description");
        clientRegistrationJson.put(ClientConstant.DURATION_TYPE, "[\"TIME_BOUND\"]");
        clientRegistrationJson.put(ClientConstant.DURATION_PERIOD, "365");
        clientRegistrationJson.put(ClientConstant.LOOKBACK_PERIOD, "365");
        clientRegistrationJson.put(ClientConstant.LOGO_URI, "https://example.com/logo.png");
        clientRegistrationJson.put(ClientConstant.REGISTRY_REFERENCES, "[{\"registered_entity_id\":\"12345\",\"registry\":\"GLEIF\"}]");
        clientRegistrationJson.put(ClientConstant.INTERMEDIARIES, "[{\"name\":\"Intermediary Name\"}]");

        OAuth2RegisteredClientFDX savedClient = new OAuth2RegisteredClientFDX();
        savedClient.setId(clientId);
        savedClient.setClientUri("https://example.com");
        savedClient.setContacts("[\"support@example.com\"]");
        savedClient.setDescription("Test Description");
        savedClient.setDurationType("[\"TIME_BOUND\"]");
        savedClient.setDurationPeriod("365");
        savedClient.setLookbackPeriod("365");
        savedClient.setLogoUri("https://example.com/logo.png");
        savedClient.setRegistryReferences("[{\"registered_entity_id\":\"12345\",\"registry\":\"GLEIF\"}]");
        savedClient.setIntermediaries("[{\"name\":\"Intermediary Name\"}]");

        when(oAuth2RegisteredClientRepository.save(any(OAuth2RegisteredClientFDX.class))).thenReturn(savedClient);

        // Act
        OAuth2RegisteredClientFDX result = dcrService.saveClientRegistration(clientId, clientRegistrationJson);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getId());
        assertEquals("https://example.com", result.getClientUri());
        assertEquals("[\"support@example.com\"]", result.getContacts());
        assertEquals("Test Description", result.getDescription());
        assertEquals("[\"TIME_BOUND\"]", result.getDurationType());
        assertEquals("365", result.getDurationPeriod());
        assertEquals("365", result.getLookbackPeriod());
        assertEquals("https://example.com/logo.png", result.getLogoUri());
        assertEquals("[{\"registered_entity_id\":\"12345\",\"registry\":\"GLEIF\"}]", result.getRegistryReferences());
        assertEquals("[{\"name\":\"Intermediary Name\"}]", result.getIntermediaries());

        // Verify the repository was called with a client containing the correct ID
        ArgumentCaptor<OAuth2RegisteredClientFDX> clientCaptor = ArgumentCaptor.forClass(OAuth2RegisteredClientFDX.class);
        verify(oAuth2RegisteredClientRepository).save(clientCaptor.capture());
        assertEquals(clientId, clientCaptor.getValue().getId());
    }

}
