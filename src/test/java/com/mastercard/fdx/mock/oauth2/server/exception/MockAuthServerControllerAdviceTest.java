package com.mastercard.fdx.mock.oauth2.server.exception;

import com.mastercard.fdx.mock.oauth2.server.common.Error;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class MockAuthServerControllerAdviceTest {

	@InjectMocks
	private MockAuthServerControllerAdvice mockAuthServerControllerAdvice;

	@Test
	void testHandleUnknownExceptions(){
		ResponseEntity<Error> res = mockAuthServerControllerAdvice.handleUnknownExceptions(null, new ApiException(HttpStatus.BAD_REQUEST, ""));
		assertNotNull(res);
		assertEquals(500, res.getStatusCode().value());
	}

	@Test
	void testHandleValidationExceptions(){
		ResponseEntity<Error> res = mockAuthServerControllerAdvice.handleValidationExceptions(null, new ValidationException(""));
		assertNotNull(res);
		assertEquals(404, res.getStatusCode().value());
	}

	@Test
	void testHandleSecurityException(){
		ResponseEntity<Error> res = mockAuthServerControllerAdvice.handleSecurityException(null, new SecurityException(""));
		assertNotNull(res);
		assertEquals(401, res.getStatusCode().value());
	}

}
