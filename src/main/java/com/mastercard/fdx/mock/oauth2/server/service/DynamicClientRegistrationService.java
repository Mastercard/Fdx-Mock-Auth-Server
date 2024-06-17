package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientMetadataClaimNames;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

@Service
@Slf4j
@AllArgsConstructor
public class DynamicClientRegistrationService {

    private static final String NEW_CLIENT_REGISTER_URI = "/client/register";
    public static final String ERROR_INVALID_CLIENT_METADATA = "invalid_client_metadata";

    public static final String ERROR_UNAUTHORIZED = "unauthorized";

    private AuthServerService authServerService;

    private ApplicationProperties appProps;

    private AuthorizationValidatorService authorizationValidatorService;

    private ClientDeletionService clientDeletionService;

    private RegisteredClientRepository registeredClientRepository;

    public ResponseEntity<String> register(String clientRegistrationReq) throws ErrorResponse {
        try {
            var dcrRequest = parsePayload(clientRegistrationReq);
            String accessToken = getAccessToken();

            // 5. Post the Client Registration to Spring AS Install DCR register.
            ResponseEntity<String> resp = authServerService.registerClient(dcrRequest, accessToken);

            if (resp.getStatusCode().value() == 201) {
                resp = makeRegisterSuccessResponse(resp, null);
            }

            return resp;
        }
        catch (JSONException ex) {
            log.error("Failed to validate Client payload " , ex);
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, ex.getLocalizedMessage());
        }
    }

    public ResponseEntity<String> modify(String clientModificationReq, String authorization, String clientId) throws ErrorResponse {
        try {
            try {
                authorizationValidatorService.validate(authorization, clientId);
            } catch (ParseException | BadJOSEException | JOSEException | com.nimbusds.oauth2.sdk.ParseException e) {
                throw new ErrorResponse(ERROR_UNAUTHORIZED, e.getMessage());
            }

            var dcrRequest = parsePayload(clientModificationReq);

            RegisteredClient client = registeredClientRepository.findByClientId(clientId);
            if(client == null) {
                throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, clientId);
            }

            RegisteredClient modifiedClient = mapModifications(client, dcrRequest);
            registeredClientRepository.save(modifiedClient);

            return authServerService.getClient(clientId, authorization.replace("Bearer ", ""));
        }
        catch (JSONException ex) {
            log.error("Failed to validate Client JWT " , ex);
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, ex.getLocalizedMessage());
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
        Set<String> newScopes = new HashSet<>(redirectUris.length());
        redirectUris.iterator().forEachRemaining((scopeObj -> newScopes.add(scopeObj.toString())));

        return (scopes) -> {
            scopes.clear();
            scopes.addAll(newScopes);
        };
    }

    public ResponseEntity<String> get(String clientId, String dhDcrAccessToken) throws ErrorResponse {
        return authServerService.getClient(clientId, dhDcrAccessToken);
    }

    public ResponseEntity<String> delete(String clientId, String authorization) throws ErrorResponse {
        try {
            authorizationValidatorService.validate(authorization, clientId);
        } catch (ParseException | BadJOSEException | JOSEException | com.nimbusds.oauth2.sdk.ParseException e) {
            throw new ErrorResponse(ERROR_UNAUTHORIZED, e.getMessage());
        }

        String deletedClientId = clientDeletionService.deleteClient(clientId);

        if (deletedClientId == null) {
            throw new ErrorResponse(ERROR_INVALID_CLIENT_METADATA, deletedClientId);
        }

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

    private static ResponseEntity<String> makeRegisterSuccessResponse(ResponseEntity<String> resp, JSONObject ssa) {
        var dcrResp = new JSONObject(resp.getBody());

        // Alter the Reg Client Uri to reflect the DCRController path.
        var regClientUri = dcrResp.optString(OidcClientMetadataClaimNames.REGISTRATION_CLIENT_URI);
        if (Strings.isNotBlank(regClientUri)) {
            regClientUri = regClientUri.replace(AuthServerService.CONNECT_REGISTER_URI, NEW_CLIENT_REGISTER_URI);
            dcrResp.put(OidcClientMetadataClaimNames.REGISTRATION_CLIENT_URI, regClientUri);
        }

        return ResponseEntity
                .status(resp.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dcrResp.toString());
    }
}
