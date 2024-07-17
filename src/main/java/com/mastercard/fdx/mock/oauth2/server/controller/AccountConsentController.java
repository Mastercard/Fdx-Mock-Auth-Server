package com.mastercard.fdx.mock.oauth2.server.controller;

import java.util.Arrays;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.consent.AccountConsentResponse;
import com.mastercard.fdx.mock.oauth2.server.service.AccountConsentService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/consent")
public class AccountConsentController {

    @Autowired
    AccountConsentService accountConsentService;

    /**
     * Below API loads the consent related information using requestUri or jwt requestObj. Called by UI.
     * @param clientId
     * @param scope
     * @param state
     * @param requestUri
     * @param requestObj
     * @return
     */
    @GetMapping("")
    public ModelAndView requestAccountConsent(
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state,
            @RequestParam(required=false, name= ApplicationConstant.OAUTH2_PARAM_REQUEST_URI) String requestUri,
            @RequestParam(required=false, name= ApplicationConstant.OAUTH2_PARAM_REQUEST) String requestObj) {
        clientId = HtmlUtils.htmlEscape(clientId);
        scope = HtmlUtils.htmlEscape(scope);
        state = HtmlUtils.htmlEscape(state);
        return accountConsentService.requestAccountConsent(clientId, scope, state, requestUri, requestObj);
    }

    /**
     * Below API is used to register consent after the user selects accounts and submits. Called by UI.
     * @param clientId
     * @param scopes
     * @param state
     * @param accountIds
     * @param consentShareDurationSeconds
     * @param prevConsentId
     * @param cancelConsent
     * @param cookie
     * @return
     */
    @PostMapping(value = "", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<String> registerAccountConsent(
            @RequestParam("clientId") String clientId,
            @RequestParam("scopes") String scopes,
            @RequestParam("state") String state,
            @RequestParam("accountIds") String accountIds,
            @RequestParam("consentShareDurationSeconds") final long consentShareDurationSeconds,
            @RequestParam("prevConsentId") String prevConsentId,
            @RequestParam("cancelConsent") String cancelConsent,
            @RequestHeader(name = HttpHeaders.COOKIE) String cookie) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        String safeCookie = Jsoup.clean(StringEscapeUtils.escapeHtml4(cookie), Safelist.basic());
        String safeAccountIds = Jsoup.clean(StringEscapeUtils.escapeHtml4(accountIds), Safelist.basic());
        String safeState = Jsoup.clean(StringEscapeUtils.escapeHtml4(state), Safelist.basic());
        String safeClientId = Jsoup.clean(StringEscapeUtils.escapeHtml4(clientId), Safelist.basic());
        String safeScopes = Jsoup.clean(StringEscapeUtils.escapeHtml4(scopes), Safelist.basic());
        String safePrevConsentId = Jsoup.clean(StringEscapeUtils.escapeHtml4(prevConsentId), Safelist.basic());
        String safeCancelConsent = Jsoup.clean(StringEscapeUtils.escapeHtml4(cancelConsent), Safelist.basic());
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserId = authentication.getName();
            AccountConsentResponse accountConsent = null;
            if(!safeCancelConsent.equalsIgnoreCase("user_cancelled_consent")) {
                accountConsent = accountConsentService.registerConsent(safePrevConsentId, currentUserId, Arrays.asList(safeAccountIds.split(",")),
                        consentShareDurationSeconds);
            }

            return accountConsentService.authoriseWithConsent(safeCookie, safeClientId, safeScopes, safeState, safeAccountIds, accountConsent, safeCancelConsent);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Below API is used to retrieve list of accounts associated with User for consent journey. Called by UI.
     * @return
     */
    @GetMapping("/accounts")
    public ResponseEntity<String> listAccountsRequiringConsent() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserId = authentication.getName();
            ResponseEntity<String> response = accountConsentService.getAccounts(currentUserId);
            return response;
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

}
