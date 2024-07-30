package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.consent.ConsentGrant;
import com.mastercard.fdx.mock.oauth2.server.service.AuthorizationValidatorService;
import com.mastercard.fdx.mock.oauth2.server.service.ConsentService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentControllerTest {

    @Mock
    ConsentService consentService;

    @Mock
    AuthorizationValidatorService authorizationValidatorService;

    @InjectMocks
    ConsentController consentController;

    @Test
    void testValidRequest() throws ErrorResponse {
        when(consentService.getConsent(anyString())).thenReturn(new ConsentGrant());
        ResponseEntity<ConsentGrant> res = consentController.getConsent("BLAH", "Auth");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testInvalidRequest() throws ErrorResponse {
        when(consentService.getConsent(anyString())).thenThrow(new SecurityException("Invalid auth token"));
        assertThrows(SecurityException.class, () -> consentController.getConsent("BLAH", "Auth"));
    }

    @Test
    void testValidRevokeConsent() throws ErrorResponse {
        doNothing().when(consentService).revokeConsent(anyString(), anyString());
        ResponseEntity<ConsentGrant> res = consentController.revokeConsent("BLAH", "Auth");
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    void testInValidRequest() throws ErrorResponse, BadJOSEException, ParseException, JOSEException {
        doThrow(ParseException.class).when(authorizationValidatorService).validateAccessToken(anyString());
        assertThrows(SecurityException.class, () -> consentController.getConsent("BLAH", "Auth"));
    }

    @Test
    void testInValidRevokeConsent() throws ErrorResponse, BadJOSEException, ParseException, JOSEException {
        doThrow(ParseException.class).when(authorizationValidatorService).validateAccessToken(anyString());
        assertThrows(SecurityException.class, () -> consentController.revokeConsent("BLAH", "Auth"));
    }
}