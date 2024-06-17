package com.mastercard.fdx.mock.oauth2.server.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ErrorResponseTest {

    @Test
    void testToString() {
        ErrorResponse resp = new ErrorResponse("error", "error_description");
        assertEquals("error", resp.getError());
        assertEquals("error_description", resp.getErrorDescription());

        String respJson = resp.toString();
        assertEquals("{\"error\":\"error\",\"error_description\":\"error_description\"}", respJson);
    }

}