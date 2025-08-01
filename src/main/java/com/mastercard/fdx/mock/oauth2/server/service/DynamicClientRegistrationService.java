package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ClientConstant;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.entity.OAuth2RegisteredClientFDX;
import com.mastercard.fdx.mock.oauth2.server.repository.OAuth2RegisteredClientRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientMetadataClaimNames;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Service
@Slf4j
@AllArgsConstructor
public class DynamicClientRegistrationService {

    private static final String NEW_CLIENT_REGISTER_URI = "/fdx/v6/register/";
    public static final String ERROR_INVALID_CLIENT_METADATA = "invalid_client_metadata";

    public static final String ERROR_UNAUTHORIZED = "unauthorized";

    private AuthServerService authServerService;

    private ApplicationProperties appProps;

    private AuthorizationValidatorService authorizationValidatorService;

    private ClientDeletionService clientDeletionService;

    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    OAuth2RegisteredClientRepository oAuth2RegisteredClientRepository;

    public ResponseEntity<String> register(String clientRegistrationReq) throws ErrorResponse {
        try {
            var dcrRequest = parsePayload(clientRegistrationReq);
            String accessToken = getAccessToken();

            // 5. Post the Client Registration to Spring AS Install DCR register.
            ResponseEntity<String> resp = authServerService.registerClient(dcrRequest, accessToken);

            if (resp.getStatusCode().value() == 201) {
                resp = makeRegisterSuccessResponse(resp);
                var dcrResp = new JSONObject(resp.getBody());
                var client_id = dcrResp.optString(OidcClientMetadataClaimNames.CLIENT_ID);
                JSONObject jsonObject = new JSONObject(clientRegistrationReq);
                OAuth2RegisteredClientFDX oAuth2RegisteredClientFDX = saveClientRegistration(client_id,jsonObject);
                oAuth2RegisteredClientFDXToJSONObject(oAuth2RegisteredClientFDX,dcrResp);
                resp = ResponseEntity.status(resp.getStatusCode()).body(dcrResp.toString());
            }
            return resp;
        }
        catch (JSONException ex) {
            log.error("Failed to validate Client payload " , ex);
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, ex.getLocalizedMessage());
        }
    }
    OAuth2RegisteredClientFDX saveClientRegistration(String client_id,JSONObject clientRegistrationResponseJson) {
        OAuth2RegisteredClientFDX oAuth2RegisteredClientFDX = new OAuth2RegisteredClientFDX();
        jSONObjectToOAuth2RegisteredClientFDX(clientRegistrationResponseJson,oAuth2RegisteredClientFDX);
        oAuth2RegisteredClientFDX.setId(client_id);
        return oAuth2RegisteredClientRepository.save(oAuth2RegisteredClientFDX);
    }

    public ResponseEntity<String> modify(String clientModificationReq, String authorization, String clientId) throws ErrorResponse {
        try {
            validateAuthorization(authorization, clientId);

            var dcrRequest = parsePayload(clientModificationReq);

            RegisteredClient client = registeredClientRepository.findByClientId(clientId);
            if(client == null) {
                throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, clientId);
            }

            RegisteredClient modifiedClient = mapModifications(client, dcrRequest);
            registeredClientRepository.save(modifiedClient);
            ResponseEntity<String> clientResponse = authServerService.getClient(clientId, authorization.replace("Bearer ", ""));
            if (clientResponse.getStatusCode() == HttpStatus.OK) {
                OAuth2RegisteredClientFDX fdxClient = oAuth2RegisteredClientRepository.findById(clientId)
                        .orElseThrow(() -> new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, "Client not found: " + clientId));
                jSONObjectToOAuth2RegisteredClientFDX(dcrRequest, fdxClient);
                oAuth2RegisteredClientRepository.save(fdxClient);
                oAuth2RegisteredClientFDXToJSONObject(fdxClient,dcrRequest);
            }
            return ResponseEntity.status(clientResponse.getStatusCode()).body(dcrRequest.toString());
        }
        catch (JSONException ex) {
            log.error("Failed to validate Client JWT " , ex);
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, ex.getLocalizedMessage());
        }
    }

    private void validateAuthorization(String authorization, String clientId) throws ErrorResponse {
        try {
            authorizationValidatorService.validate(authorization, clientId);
        } catch (ParseException | BadJOSEException | JOSEException | com.nimbusds.oauth2.sdk.ParseException e) {
            throw new ErrorResponse(ERROR_UNAUTHORIZED, e.getMessage());
        }
    }

    private RegisteredClient mapModifications(RegisteredClient client, JSONObject dcrRequest) {

        return RegisteredClient.from(client)
                .clientName(dcrRequest.getString("client_name"))
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(Arrays.asList(dcrRequest.getString("scope").split(" ")));
                })
                .redirectUris(getRedirectUrlsMapping(dcrRequest))
                .build();
    }

    private static Consumer<Set<String>> getRedirectUrlsMapping(JSONObject dcrRequest) {
        JSONArray redirectUris = dcrRequest.getJSONArray("redirect_uris");
        Set<String> newRedirectUris = new HashSet<>(redirectUris.length());
        redirectUris.iterator().forEachRemaining((redirectUri -> newRedirectUris.add(redirectUri.toString())));

        return uris -> {
            uris.clear();
            uris.addAll(newRedirectUris);
        };
    }

    public ResponseEntity<String> get(String clientId, String dhDcrAccessToken) throws ErrorResponse {
        ResponseEntity<String> client = authServerService.getClient(clientId, dhDcrAccessToken);
        if (client.getStatusCode() == HttpStatus.OK) {
            OAuth2RegisteredClientFDX fdxClient = oAuth2RegisteredClientRepository.findById(clientId)
                    .orElseThrow(() -> new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, "Client not found: " + clientId));
            var dcrResp = new JSONObject(client.getBody());
            oAuth2RegisteredClientFDXToJSONObject(fdxClient, dcrResp);
            return ResponseEntity.status(client.getStatusCode()).body(dcrResp.toString());
        }
        return client;
    }

    private void jSONObjectToOAuth2RegisteredClientFDX(JSONObject json, OAuth2RegisteredClientFDX client) {
        client.setClientUri(json.optString(ClientConstant.CLIENT_URI));
        client.setContacts(json.optString(ClientConstant.CONTACTS));
        client.setDescription(json.optString(ClientConstant.DESCRIPTION));
        client.setDurationType(json.optString(ClientConstant.DURATION_TYPE));
        client.setDurationPeriod(json.optString(ClientConstant.DURATION_PERIOD));
        client.setLookbackPeriod(json.optString(ClientConstant.LOOKBACK_PERIOD));
        client.setLogoUri(json.optString(ClientConstant.LOGO_URI));
        client.setRegistryReferences(json.optString(ClientConstant.REGISTRY_REFERENCES));
        client.setIntermediaries(json.optString(ClientConstant.INTERMEDIARIES));
    }

    private void oAuth2RegisteredClientFDXToJSONObject(OAuth2RegisteredClientFDX client, JSONObject dcrResp) {
        dcrResp.put(ClientConstant.CLIENT_URI, client.getClientUri());
        if(null != client.getContacts() && !client.getContacts().isEmpty())
            dcrResp.put(ClientConstant.CONTACTS, new JSONArray(client.getContacts()));
        dcrResp.put(ClientConstant.DESCRIPTION, client.getDescription());
        if(null != client.getDurationType() && !client.getDurationType().isEmpty())
            dcrResp.put(ClientConstant.DURATION_TYPE, new JSONArray(client.getDurationType()));
        dcrResp.put(ClientConstant.DURATION_PERIOD, client.getDurationPeriod());
        dcrResp.put(ClientConstant.LOOKBACK_PERIOD, client.getLookbackPeriod());
        dcrResp.put(ClientConstant.LOGO_URI, client.getLogoUri());
        if(null != client.getRegistryReferences() && !client.getRegistryReferences().isEmpty())
            dcrResp.put(ClientConstant.REGISTRY_REFERENCES, new JSONArray(client.getRegistryReferences()));
        if(null != client.getIntermediaries() && !client.getIntermediaries().isEmpty())
            dcrResp.put(ClientConstant.INTERMEDIARIES, new JSONArray(client.getIntermediaries()));
    }

    public ResponseEntity<String> delete(String clientId, String authorization) throws ErrorResponse {
        validateAuthorization(authorization, clientId);

        String deletedClientId = clientDeletionService.deleteClient(clientId);
        if (deletedClientId == null) {
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, deletedClientId);
        }
        // delete from oauth2_registered_client_details table
        oAuth2RegisteredClientRepository.deleteById(clientId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }

    private String getAccessToken() throws ErrorResponse {
        // 4. Get DH ADR Client Registrar Access Token
        return authServerService.getAccessToken(appProps.getDhClientId(), appProps.getDhClientSecret());
    }

    private static JSONObject parsePayload(String clientRegistrationReq) throws JSONException {
        return new JSONObject(clientRegistrationReq);
    }

    private static ResponseEntity<String> makeRegisterSuccessResponse(ResponseEntity<String> resp) {
        var dcrResp = new JSONObject(resp.getBody());

        // Alter the Reg Client Uri to reflect the DCRController path.
        var regClientUri = dcrResp.optString(OidcClientMetadataClaimNames.REGISTRATION_CLIENT_URI);
        if (Strings.isNotBlank(regClientUri)) {
            regClientUri = regClientUri.replace("/connect/register?client_id=", NEW_CLIENT_REGISTER_URI);
            dcrResp.put(OidcClientMetadataClaimNames.REGISTRATION_CLIENT_URI, regClientUri);
        }

        return ResponseEntity
                .status(resp.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dcrResp.toString());
    }

}
