package com.mastercard.fdx.mock.oauth2.server.service;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.consent.ConsentGrant;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsent;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsentRepository;
import com.mastercard.fdx.mock.oauth2.server.consent.Party;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class ConsentService {

	@Autowired
	private CustomerConsentRepository customerConsentRepository;

	@Autowired
	private ApplicationProperties appProps;

	@Autowired
	private OAuth2AuthorizationService authorizationService;

	public ConsentGrant getConsent(String consentId) {
		CustomerConsent customerConsent = getCustomerConsent(consentId);
		ConsentGrant consentGrant = new ConsentGrant();
		consentGrant.setId(customerConsent.getConsentId());
		consentGrant.setStatus(customerConsent.getStatus());
		consentGrant.setCreatedTime(customerConsent.getCreatedDate());
		consentGrant.setExpirationTime(customerConsent.getEndDate());
		consentGrant.setParties(Arrays.asList(createDP()));
		return consentGrant;
	}

	public void revokeConsent(String consentId, String token){
		CustomerConsent customerConsent = getCustomerConsent(consentId);
		OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
		try {
			authorizationService.remove(authorization);
		} catch (IllegalArgumentException ex){
			throw new ValidationException("Consent already revoked");
		}
		customerConsentRepository.updateCustomerConsent(consentId, "REVOKED");
	}

	private CustomerConsent getCustomerConsent(String consentId){
		CustomerConsent customerConsent = customerConsentRepository.findByConsentId(consentId);
		if(customerConsent == null)
			throw new ValidationException("Consent Id not found");
		return customerConsent;
	}

	private Party createDP(){
		Party dp = new Party();
		dp.setName("FDX mock bank");
		dp.setType("DATA_PROVIDER");
		dp.setHomeUri(appProps.getAuthServerBaseSecureUrl());
		dp.setLogoUri(appProps.getAuthServerBaseSecureUrl());
		dp.setRegistry("FDX");
		dp.setRegisteredEntityName("FDX mock bank");
		dp.setRegisteredEntityId("54321FDX001DP001");
		return dp;
	}
}
