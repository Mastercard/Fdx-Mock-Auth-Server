package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.consent.ConsentGrant;
import com.mastercard.fdx.mock.oauth2.server.service.AuthorizationValidatorService;
import com.mastercard.fdx.mock.oauth2.server.service.ConsentService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.ParseException;

@Controller
@Slf4j
@RequestMapping("/fdx/v6/consents")
public class ConsentController {

	@Autowired
	ConsentService consentService;

	@Autowired
	AuthorizationValidatorService authorizationValidatorService;

	/**
	 * Retrieves the consent data based on the consentId
	 * @param consentId
	 * @param authorization
	 * @return
	 */
	@GetMapping("/{consentId}")
	public ResponseEntity<ConsentGrant> getConsent(@PathVariable(name = "consentId") String consentId, @RequestHeader("Authorization") String authorization){
		try {
			authorizationValidatorService.validateAccessToken(authorization);
		} catch (ParseException | BadJOSEException | JOSEException | com.nimbusds.oauth2.sdk.ParseException e) {
			throw new SecurityException("Invalid or expired access token");
		}
		return new ResponseEntity<>(consentService.getConsent(consentId), HttpStatus.OK);
	}

	/**
	 * Revokes the consent based on the consentId
	 * @param consentId
	 * @param authorization
	 * @return
	 */
	@PutMapping ("/{consentId}/revocation")
	public ResponseEntity<ConsentGrant> revokeConsent(@PathVariable(name = "consentId") String consentId, @RequestHeader("Authorization") String authorization){
		try {
			authorizationValidatorService.validateAccessToken(authorization);
		} catch (ParseException | BadJOSEException | JOSEException | com.nimbusds.oauth2.sdk.ParseException e) {
			throw new SecurityException("Invalid or expired access token");
		}
		consentService.revokeConsent(consentId, authorization);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
