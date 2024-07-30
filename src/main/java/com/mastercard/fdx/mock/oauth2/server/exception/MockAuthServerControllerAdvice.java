package com.mastercard.fdx.mock.oauth2.server.exception;

import com.mastercard.fdx.mock.oauth2.server.common.Error;
import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This class is the handler for exceptions occurred in APIs, this will intercept the exceptions,
 *  add necessary logs and send the response in custom format.
 */
@Slf4j
@ControllerAdvice
public class MockAuthServerControllerAdvice {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Error> handleUnknownExceptions(HttpServletRequest request, final Exception ex) {
        logGenericException(ex);
        return getError(HttpStatus.INTERNAL_SERVER_ERROR, "We have encountered some internal error in processing your request.", ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<Error> handleValidationExceptions(HttpServletRequest request, final ValidationException ex) {
        logGenericException(ex);
        return getError(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public final ResponseEntity<Error> handleSecurityException(HttpServletRequest request, final SecurityException ex) {
        logGenericException(ex);
        return getError(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public final ResponseEntity<Error> handleMissingRequestHeaderException(HttpServletRequest request, final MissingRequestHeaderException ex) {
        logGenericException(ex);
        return getError(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
    }
    private void logGenericException(Exception ex) {
        log.error("Error name: {}, Error Message: {}, Stack trace: {}!",
                ex.getClass().getSimpleName(), ex.getMessage(),
                CommonUtils.getStringOfStackTrace(ex.getStackTrace()));
    }

    private ResponseEntity<Error> getError(HttpStatus status, String errMsg, String errDesc) {
        Error error = new Error(errMsg, errDesc);
        return new ResponseEntity<>(error, status);
    }

}
