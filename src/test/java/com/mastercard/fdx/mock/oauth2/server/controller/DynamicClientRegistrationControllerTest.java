package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.service.DynamicClientRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicClientRegistrationControllerTest {

    @Mock
    DynamicClientRegistrationService dcrService;

    @InjectMocks
    DynamicClientRegistrationController dcrController;

    @Test
    void testValidRequest() throws ErrorResponse {
        when(dcrService.register(anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<String> res = dcrController.register("BLAH");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testInvalidRequest() throws ErrorResponse {
        String errMsg = "TEST_ERROR";
        when(dcrService.register(anyString())).thenThrow(new ErrorResponse(errMsg, "error_description"));
        ResponseEntity<String> res = dcrController.register("BLAH");
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().contains(errMsg));
    }

    @Test
    void testGetRequest() throws ErrorResponse {
        when(dcrService.get(anyString(), eq("abcd"))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<String> res = dcrController.get("BLAH", "Bearer abcd");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testDeleteRequest() throws ErrorResponse {
        when(dcrService.delete(anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<String> res = dcrController.delete("BLAH", "abcd");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testModifyRequest() throws ErrorResponse {
        when(dcrService.modify(anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<String> res = dcrController.modify("BLAH", "clientId","Bearer abcd");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }
}