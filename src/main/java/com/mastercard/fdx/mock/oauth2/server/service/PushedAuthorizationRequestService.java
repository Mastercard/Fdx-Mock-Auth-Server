package com.mastercard.fdx.mock.oauth2.server.service;

import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.RemoteJWKSSetHelper;
import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.PushedAuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationRequest;
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.rar.AuthorizationDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class PushedAuthorizationRequestService {

    public static final String INVALID_PAR_REQUEST = "INVALID_PAR_REQUEST";
    public static final int DEFAULT_DURATION = 90;
    public static final String INVALID_CLIENT = "INVALID_CLIENT";
    public static final String DUMMY_URL_VAL = "HTTP://SOMETHING.COM/URL";

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private PushAuthorizationRequestRepository pushAuthorizationRequestRepository;

    @Autowired
    RemoteJWKSSetHelper remoteJWKSSetHelper;

    /**
     * Below method validates the Pushed Authorization request, stores the metadata about consent and sends the unique requestUri
     * This requestUri is then used with /authorize consent url.
     * @param body
     * @return
     */
    public PushedAuthorizationResponse processPAR(String body) {
        try {
            var parReq = parsePAR(body);

            var client = registeredClientRepository.findByClientId(parReq.getAuthorizationRequest().getClientID().getValue());
            if (client == null) {
                return new PushedAuthorizationErrorResponse(new ErrorObject(INVALID_CLIENT, "Invalid Client."));
            }

            validateRequestJws(parReq, client.getClientSettings().getJwkSetUrl());

            var parData = new PushAuthorizationRequestData(DEFAULT_DURATION, parReq.getAuthorizationRequest().toQueryString());
            setAuthorizationDetails(parData, parReq);
            pushAuthorizationRequestRepository.save(parData);

            return new PushedAuthorizationSuccessResponse(URI.create(parData.getRequestUri()), parData.getDuration());

        } catch (Exception e) {
            log.error("INVALID_PAR_REQUEST - " + e.getLocalizedMessage());
            return new PushedAuthorizationErrorResponse(new ErrorObject(INVALID_PAR_REQUEST, e.getLocalizedMessage()));
        }

    }

    private void setAuthorizationDetails(PushAuthorizationRequestData parData, PushedAuthorizationRequest parReq) {
        List<AuthorizationDetail> authDetails = parReq.getAuthorizationRequest().getAuthorizationDetails();
        if(!CollectionUtils.isEmpty(authDetails)){
            parData.setAuthorizationDetails(parReq.getAuthorizationRequest().getAuthorizationDetails().get(0).toJSONObject().toString());
        }
    }

    public static PushedAuthorizationRequest parsePAR(String body) throws ParseException {
        var req = new HTTPRequest(HTTPRequest.Method.POST, URI.create(DUMMY_URL_VAL));
        req.setContentType(ContentType.APPLICATION_URLENCODED.getType());
        req.setBody(body);
        return PushedAuthorizationRequest.parse(req);
    }

    public static PushedAuthorizationRequest parsePAR(String clientId, String requestObj) throws ParseException {
            return PushedAuthorizationRequestService.parsePAR(
                    OAuth2ParameterNames.CLIENT_ID + "=" + clientId + "&" +
                            ApplicationConstant.OAUTH2_PARAM_REQUEST + "=" + requestObj);
    }

    /**
     * Below method is used to validate the jwt PushedAuthorizationRequest with the client public jwks.
     * @param parReq
     * @param clientJwksUri
     * @throws BadJOSEException
     * @throws JOSEException
     */
    private void validateRequestJws(PushedAuthorizationRequest parReq, String clientJwksUri) throws BadJOSEException, JOSEException {

        JWKSource<SecurityContext> keySource = remoteJWKSSetHelper.getRemoteJWKSet(clientJwksUri);
        JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.PS256, keySource);

        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.process(parReq.getAuthorizationRequest().getRequestObject(), null);
    }

}
