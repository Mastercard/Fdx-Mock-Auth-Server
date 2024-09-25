package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.service.PushedAuthorizationRequestService;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.PushedAuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class PushAuthorizationRequestControllerTest {

    @Mock
    PushedAuthorizationRequestService parService;

    @InjectMocks
    PushedAuthorizationRequestController parController;

    @Test
    void testValidRequest() {
        PushedAuthorizationSuccessResponse successResponse = new PushedAuthorizationSuccessResponse(URI.create("TEST_URI"), 90);
        Mockito.when(parService.processPAR(Mockito.any())).thenReturn(successResponse);

        ResponseEntity<JSONObject> res = parController.handlePushedAuthorizationRequest(null);
        assertEquals("TEST_URI", res.getBody().getAsString("request_uri"));
        assertEquals("90", res.getBody().getAsString("expires_in"));
        assertEquals(201, res.getStatusCode().value());
    }

    @Test
    void testInvalidRequest() {
        PushedAuthorizationErrorResponse errorResponse = new PushedAuthorizationErrorResponse(new ErrorObject("CODE", "DESCRIPTION"));
        Mockito.when(parService.processPAR(Mockito.any())).thenReturn(errorResponse);

        ResponseEntity<JSONObject> res = parController.handlePushedAuthorizationRequest(null);
        assertEquals("CODE", res.getBody().getAsString("error"));
        assertEquals("DESCRIPTION", res.getBody().getAsString("error_description"));
    }
}