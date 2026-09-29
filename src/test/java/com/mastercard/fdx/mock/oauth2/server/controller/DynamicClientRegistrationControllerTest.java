package com.mastercard.fdx.mock.oauth2.server.controller;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.service.DynamicClientRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class DynamicClientRegistrationControllerTest {

    @Mock
    DynamicClientRegistrationService dcrService;

    @InjectMocks
    DynamicClientRegistrationController dcrController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(dcrController).build();
    }

    @Test
    void testValidRequest() throws ErrorResponse {
        when(dcrService.register(anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ResponseEntity<String> res = dcrController.register("BLAH");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testInvalidRequest() throws ErrorResponse {
        when(dcrService.register(anyString())).thenThrow(
                new ErrorResponse(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA,
                        "client_name is required"));

        ResponseEntity<String> res = dcrController.register("BLAH");

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        JSONObject error = new JSONObject(res.getBody());
        assertEquals(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA, error.getString("error"));
        assertEquals("client_name is required", error.getString("error_description"));
    }

    @Test
    void testEmptyHttpRequestBody() throws Throwable {
        when(dcrService.register(isNull())).thenThrow(
                new ErrorResponse(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA,
                        "Request body is required"));

        mockMvc.perform(post("/fdx/v6/register").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(DynamicClientRegistrationService.ERROR_INVALID_CLIENT_METADATA))
                .andExpect(jsonPath("$.error_description").value("Request body is required"));
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
