package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.consent.AccountConsentResponse;
import com.mastercard.fdx.mock.oauth2.server.service.AccountConsentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AccountConsentControllerTest {

    @Mock
    private AccountConsentService acs;

    @InjectMocks
    private AccountConsentController accountConsentController;

    @Test
    void testAnonymousValidation() {
        AnonymousAuthenticationToken token = Mockito.mock(AnonymousAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(token);

        ResponseEntity<String> res =  accountConsentController.registerAccountConsent("clientId", "scopes", "state", "accountId1,accountId2", 365, "prevCdrArrangementId", "prevCdrArrangementId", "cookie");
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());

        res =  accountConsentController.listAccountsRequiringConsent();
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());

    }

    @Test
    void testRegisterAccountConsent() {
        UsernamePasswordAuthenticationToken token = Mockito.mock(UsernamePasswordAuthenticationToken.class);
        Mockito.when(token.getName()).thenReturn("USER1");
        SecurityContextHolder.getContext().setAuthentication(token);

        AccountConsentResponse accConsentRes = new AccountConsentResponse("customerId", "cdrArrangementId", new Timestamp(1));
        Mockito.when(acs.registerConsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.anyLong())).thenReturn(accConsentRes);
        Mockito.when(acs.authoriseWithConsent(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.anyString())).thenReturn(new ResponseEntity<> ("", HttpStatus.OK));

        ResponseEntity<String> res =  accountConsentController.registerAccountConsent("clientId", "scopes", "state", "accountId1,accountId2", TimeUnit.DAYS.toSeconds(365), "prevCdrArrangementId", "prevCdrArrangementId","cookie");
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void testListAccountsRequiringConsent() {
        UsernamePasswordAuthenticationToken token = Mockito.mock(UsernamePasswordAuthenticationToken.class);
        Mockito.when(token.getName()).thenReturn("USER1");
        SecurityContextHolder.getContext().setAuthentication(token);

        ResponseEntity<String> res =  accountConsentController.listAccountsRequiringConsent();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(Objects.requireNonNull(res.getBody()).contains("LOAN_ACCOUNT"));
    }

}
