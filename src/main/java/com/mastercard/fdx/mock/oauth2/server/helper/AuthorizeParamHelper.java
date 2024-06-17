package com.mastercard.fdx.mock.oauth2.server.helper;

import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.utils.RequestWrapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.PushedAuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.StringUtils;

@Slf4j
public class AuthorizeParamHelper {
    private AuthorizeParamHelper() {}
    public static void injectRequiredAuthorizeParameters(PushedAuthorizationRequest par, RequestWrapper request) throws java.text.ParseException {
        // Obtaining the following parameters from the Claims Set.
        JWTClaimsSet claims = par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet();
        setRequestParam(request, PkceParameterNames.CODE_CHALLENGE, claims);
        setRequestParam(request, PkceParameterNames.CODE_CHALLENGE_METHOD, claims);
        setRequestParam(request, OAuth2ParameterNames.REDIRECT_URI, claims);
        setRequestParam(request, OAuth2ParameterNames.RESPONSE_TYPE, claims);
        setRequestParam(request, ApplicationConstant.OAUTH2_PARAM_RESPONSE_MODE, claims);
        setRequestParam(request, OAuth2ParameterNames.STATE, claims);
        setRequestParam(request, OAuth2ParameterNames.SCOPE, claims);
        setRequestParam(request, ApplicationConstant.OAUTH2_PARAM_REQUEST, par.getAuthorizationRequest().getRequestObject().serialize());
    }

    private static void setRequestParam(HttpServletRequest request, String paramName, JWTClaimsSet claims) throws java.text.ParseException {
        String paramValue = claims.getStringClaim(paramName);
        setRequestParam(request, paramName, paramValue);
    }

    private static void setRequestParam(HttpServletRequest request, String paramName, String paramValue) {
        if (StringUtils.hasText(paramValue)) {
            String prevVal = request.getParameter(paramName);
            request.getParameterMap().put(paramName, ArrayUtils.toArray(paramValue));

            log.info("Adding Authorize ParamName: [" + paramName + "], PrevVal: [" + prevVal + "] - NewVal: [" + paramValue + "].");
        }
    }
}
