package com.mastercard.fdx.mock.oauth2.server.utils;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.service.PushedAuthorizationRequestService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.PushedAuthorizationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OAuth2AuthorizationRequestUtils {

    public static final long ONE_TIME_CONTENT_SHARING_DURATION = TimeUnit.MINUTES.toSeconds(5);
    public static final long MAX_SHARING_DURATION = TimeUnit.DAYS.toSeconds(365);

    private OAuth2AuthorizationRequestUtils() {
    }

    public static long adjustSharingDuration(long sharingDuration) {
        // Ensure sharingDuration is between ONE_TIME_CONTENT_SHARING_DURATION and MAX_SHARING_DURATION
        sharingDuration = Math.max(sharingDuration, ONE_TIME_CONTENT_SHARING_DURATION);
        sharingDuration = Math.min(sharingDuration, MAX_SHARING_DURATION);
        return sharingDuration;
    }

    public static long getSharingDuration(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest.getAdditionalParameters().containsKey("request")) {
            try {
                String requestObj = (String) authorizationRequest.getAdditionalParameters().get("request");
                PushedAuthorizationRequest par = PushedAuthorizationRequestService.parsePAR(authorizationRequest.getClientId(), requestObj);
                JWTClaimsSet requestPayload = par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet();
                if (requestPayload.getClaim(ApplicationConstant.PAR_CLAIMS) != null) {
                    JSONObject claims = new JSONObject(requestPayload.getJSONObjectClaim(ApplicationConstant.PAR_CLAIMS));
                    return claims.optLong(ApplicationConstant.PAR_CLAIMS_SHARING_DURATION, -1);
                }
            } catch (ParseException | java.text.ParseException e) {
                log.error("Invalid PAR Request.", e);
            }
        }
        return -1;
    }

    public static boolean isOneTimeConsentRequest(OAuth2AuthorizationRequest authorizationRequest) {
        long sharingDuration = OAuth2AuthorizationRequestUtils.getSharingDuration(authorizationRequest);
        if (sharingDuration == 0) {
            log.debug("isOneTimeConsentRequest(): TRUE");
            return true;
        }
        log.debug("isOneTimeConsentRequest(): FALSE");
        return false;
    }

    public static OAuth2AuthorizationRequest getOAuth2AuthorizationRequest(OAuth2TokenContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        if (authorization != null)
            return ((OAuth2AuthorizationRequest) authorization.getAttributes().getOrDefault(OAuth2AuthorizationRequest.class.getName(), null));
        return null;
    }
}
