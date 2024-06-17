package com.mastercard.fdx.mock.oauth2.server.service;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.authorization.claims.Oauth2TokenClaimNames;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER;

@Service
public class AuthServerService {

    public static final String OAUTH2_TOKEN_URI = "/oauth2/token";
    public static final String OAUTH2_INTROSPECT_URI = "/oauth2/introspect";
    public static final String CLIENT_SCOPE_CREATE = "client.create";
    public static final String AUTHENTICATION_ERROR = "authentication_error";
    public static final String CLIENT_SCOPE_READ = "client.read";

    public static final String CONNECT_REGISTER_URI = "/connect/register";

    @Autowired
    private ApplicationProperties appProps;

    public ResponseEntity<String> registerClient(JSONObject req, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set(ApplicationConstant.SSL_CLIENT_VERIFY, ApplicationConstant.SUCCESS);
        HttpEntity<String> request = new HttpEntity<>(req.toString(), headers);
        return restTemplate.postForEntity(appProps.getLocalServerBaseUri() + CONNECT_REGISTER_URI, request, String.class);
    }

    public String getAccessToken(String clientId, String clientSecret) throws ErrorResponse {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(Oauth2TokenClaimNames.GRANT_TYPE, AuthorizationGrantType.CLIENT_CREDENTIALS.getValue());
        map.add(Oauth2TokenClaimNames.SCOPE, CLIENT_SCOPE_CREATE);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(appProps.getLocalServerBaseUri() + OAUTH2_TOKEN_URI, request, String.class);
        if (responseEntityStr.getStatusCodeValue() != 200) {
            throw new ErrorResponse(AUTHENTICATION_ERROR, responseEntityStr.getStatusCodeValue() + " - " + responseEntityStr.getBody());
        }
        JSONObject tokenResp = new JSONObject(responseEntityStr.getBody());

        return tokenResp.getString(OAuth2TokenType.ACCESS_TOKEN.getValue());
    }

    public ResponseEntity<String> getClient(String clientId, String dhDcrAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(dhDcrAccessToken);
        
        try {
            return restTemplate.exchange(appProps.getLocalServerBaseUri() + CONNECT_REGISTER_URI + "?client_id=" + clientId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(
                    e.getResponseBodyAsString(),
                    e.getResponseHeaders(),
                    e.getStatusCode());
        }
    }

}
