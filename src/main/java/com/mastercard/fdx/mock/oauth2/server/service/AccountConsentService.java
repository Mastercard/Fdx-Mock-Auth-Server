package com.mastercard.fdx.mock.oauth2.server.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.consent.AccountConsentResponse;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsent;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsentRepository;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.CommonUtils;
import com.mastercard.fdx.mock.oauth2.server.utils.OAuth2AuthorizationRequestUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.ParseException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountConsentService {
    public static final String CONSENT_URI = "/consent";
    public static final String CONSENT_ACCOUNTS_URI = "/consent/accounts?userId={userId}";
    public static final String USER_ID = "userId";
    public static final long DEFAULT_SHARING_DURATION = OAuth2AuthorizationRequestUtils.MAX_SHARING_DURATION;
    public static final String PARAM_AMEND_CONSENT = "paramAmendConsent";
    public static final String PARAM_PREV_ACCOUNT_IDS = "paramPrevAccountIds";
    public static final String PARAM_SHARING_DURATION = "paramSharingDuration";

    public static final String PARAM_PREV_CONSENT_ID = "paramPrevConsentId";

    @Autowired
    private PushAuthorizationRequestRepository parRepo;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private CustomerConsentRepository customerConsentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationProperties appProps;

    public ModelAndView requestAccountConsent(String clientId, String scope, String state, String requestUri, String requestObj) {
        var mav = new ModelAndView("index");

        mav.getModelMap().addAttribute("paramClientId", clientId);
        mav.getModelMap().addAttribute("paramState", state);
        mav.getModelMap().addAttribute("paramScopes", scope);

        if (StringUtils.hasText(requestUri)) {
            ModelAndView mav1 = extractParamsFromRequestUri(requestUri, mav);
            if (mav1 != null) return mav1;
        }

        mav.getModelMap().addAttribute(PARAM_AMEND_CONSENT, false);
        mav.getModelMap().addAttribute(PARAM_PREV_CONSENT_ID, "");
        mav.getModelMap().addAttribute(PARAM_PREV_ACCOUNT_IDS, "");

        if (StringUtils.hasText(requestObj)) {
            extractParamsFromRequestObj(clientId, requestObj, mav);
        } else {
            mav.getModelMap().addAttribute(PARAM_SHARING_DURATION, DEFAULT_SHARING_DURATION);
        }

        return mav;
    }

    private void extractParamsFromRequestObj(String clientId, String requestObj, ModelAndView mav) {
        try {
            var par = PushedAuthorizationRequestService.parsePAR(clientId, requestObj);
            if (par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet().getClaim(ApplicationConstant.PAR_CLAIMS) != null) {

                var json = new JSONObject(par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet().getJSONObjectClaim(ApplicationConstant.PAR_CLAIMS));
                long sharingDuration = getSharingDuration(json);
                mav.getModelMap().addAttribute(PARAM_SHARING_DURATION, Long.toString(sharingDuration));
            }
        } catch (ParseException | java.text.ParseException e) {
            log.error("Invalid PAR Request - Ignoring.", e);
        }
    }

    private ModelAndView extractParamsFromRequestUri(String requestUri, ModelAndView mav) {
        PushAuthorizationRequestData parData = parRepo.findByRequestUri(requestUri);
        if ((parData != null) && !parData.isExpired()) {
            try {
                var par = PushedAuthorizationRequestService.parsePAR(parData.getRequestParams());

                if (par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet().getClaim(ApplicationConstant.PAR_CLAIMS) != null) {
                    var json = new JSONObject(par.getAuthorizationRequest().getRequestObject().getJWTClaimsSet().getJSONObjectClaim(ApplicationConstant.PAR_CLAIMS));
                    var consentId = json.optString(ApplicationConstant.PAR_CLAIMS_CONSENT_ID, "");
                    long sharingDuration = getSharingDuration(json);
                    CustomerConsent customerConsent =null;
                    if(org.apache.commons.lang3.StringUtils.isNotBlank(consentId)) {
                        customerConsent = customerConsentRepository.findByConsentId(consentId);
                    }
                    if (customerConsent != null) {
                        mav.getModelMap().addAttribute(PARAM_AMEND_CONSENT, true);
                        mav.getModelMap().addAttribute(PARAM_SHARING_DURATION, sharingDuration);
                        mav.getModelMap().addAttribute(PARAM_PREV_CONSENT_ID, consentId);
                        mav.getModelMap().addAttribute(PARAM_PREV_ACCOUNT_IDS, customerConsent.getAccountIds());
                    } else {
                        mav.getModelMap().addAttribute(PARAM_AMEND_CONSENT, false);
                        mav.getModelMap().addAttribute(PARAM_SHARING_DURATION, sharingDuration);
                        mav.getModelMap().addAttribute(PARAM_PREV_CONSENT_ID, "");
                        mav.getModelMap().addAttribute(PARAM_PREV_ACCOUNT_IDS, "");
                    }

                    return mav;
                }
            } catch (ParseException | java.text.ParseException e) {
                log.error("Invalid PAR Request - Ignoring.", e);
            }
        } else {
            log.error("RequestUri not located - Ignoring.");
        }
        return null;
    }

    private long getSharingDuration(JSONObject json) {
        var sharingDuration = json.optLong(ApplicationConstant.PAR_CLAIMS_SHARING_DURATION, DEFAULT_SHARING_DURATION);
        return OAuth2AuthorizationRequestUtils.adjustSharingDuration(sharingDuration);
    }

    public ResponseEntity<String> getAccounts(String userId) {
      try {
    	var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.AUTHORIZATION, appProps.getResourceServerAuthCode());

        Map<String, String> params = new HashMap<>();
        params.put(USER_ID, userId);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(
                appProps.getMockResServerBaseUrl() + CONSENT_ACCOUNTS_URI,
                HttpMethod.GET,
                request,
                String.class,
                params);
      }
      catch (Exception e) {
		e.printStackTrace();
	}
      return null;
    }

    public AccountConsentResponse registerConsent(String prevConsentId, String userId, List<String> accountIds, long consentShareDurationSeconds) {
        var req = new JSONObject();
        req.put("consentId", prevConsentId);
        req.put("accountIds", accountIds);
        req.put(USER_ID, userId);
        req.put("consentShareDurationSeconds", consentShareDurationSeconds);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.AUTHORIZATION, appProps.getResourceServerAuthCode());

        HttpEntity<String> request = new HttpEntity<>(req.toString(), headers);
        if(!StringUtils.hasText(prevConsentId)) {
            return restTemplate.postForObject(appProps.getMockResServerBaseUrl() + CONSENT_URI, request, AccountConsentResponse.class);
        } else {
            restTemplate.put(appProps.getMockResServerBaseUrl() + CONSENT_URI + "/" + prevConsentId, request, AccountConsentResponse.class);
            var response = new AccountConsentResponse();
            response.setCustomerId(null);// We don't have the customer ID
            response.setConsentId(prevConsentId);
            var endDate = new Timestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(consentShareDurationSeconds));
            response.setEndDate(endDate);//We assume that the end date will be set as requested
            return response;
        }
    }

    public ResponseEntity<String> authoriseWithConsent(
            String cookie, String clientId, String scopes, String state,
            String accountIds, AccountConsentResponse accountConsent, String safeCancelConsent) {

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.COOKIE, cookie);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("state", state);
        params.put("scope", Arrays.asList(scopes.split(" ")));
        if(accountConsent != null) {
            params.add("endDate", Long.toString(accountConsent.getEndDate().getTime()));
            params.add("consentId", accountConsent.getConsentId());
        }
        params.add("accountIds", accountIds);
        params.add("cancelConsent", safeCancelConsent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        return restTemplate.postForEntity(appProps.getLocalServerBaseUri() + ApplicationConstant.DEFAULT_AUTHORIZATION_ENDPOINT_URI, request, String.class);
    }

    public boolean exists(String consentId) {
        CustomerConsent customerConsent = customerConsentRepository.findByConsentId(consentId);
        return customerConsent != null;
    }

    public boolean validateClientAssertion(String clientId, String clientAssertionType, String clientAssertion) {
        try {
            if (!clientAssertionType.equalsIgnoreCase(ApplicationConstant.OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER)) {
                log.error("Invalid client assertion type [" + clientAssertionType + "]");
                return false;
            }

            JWSObject clientAssertionJwt = JWSObject.parse(clientAssertion);
            var request = new JSONObject(clientAssertionJwt.getPayload().toString());
            var requestClientId = request.optString(JWTClaimNames.ISSUER, "");
            if (!clientId.equalsIgnoreCase(requestClientId)) {
                log.error("Client ID: [" + clientId + "] does not match Assertion Issuer: [" + requestClientId + "]");
                return false;
            }

            var client = registeredClientRepository.findByClientId(clientId);
            if (client == null) {
                log.error("No client with client ID: " + clientId);
                return false;
            }

            return validateAssertion(clientAssertion, client);

        } catch (java.text.ParseException e) {
            log.error("Failed to parse clientAssertion: " + clientAssertion, e);
            return false;
        }
    }

    private boolean validateAssertion(String clientAssertion, RegisteredClient client) {
        RemoteJWKSet<SecurityContext> keySource = CommonUtils.getRemoteJWKSet(client.getClientSettings().getJwkSetUrl());
        JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.PS256, keySource);
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);
        try {
            jwtProcessor.process(clientAssertion, null);
        } catch (java.text.ParseException | BadJOSEException | JOSEException e) {
            log.error("Invalid auth header", e);
            return false;
        }
        return true;
    }

}