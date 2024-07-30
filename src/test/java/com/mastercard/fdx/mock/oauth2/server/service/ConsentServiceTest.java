package com.mastercard.fdx.mock.oauth2.server.service;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsent;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsentRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

	@InjectMocks
	ConsentService consentService;

	@Mock
	private CustomerConsentRepository customerConsentRepository;

	@Mock
	private ApplicationProperties appProps;

	@Mock
	private OAuth2AuthorizationService authorizationService;

	@Test
	void testGetConsent(){
		when(customerConsentRepository.findByConsentId(anyString())).thenReturn(new CustomerConsent());
		assertNotNull(consentService.getConsent("consentId"));
	}

	@Test
	void testGetConsentNotFound(){
		when(customerConsentRepository.findByConsentId(anyString())).thenThrow(new ValidationException("Consent Id not found"));
		assertThrows(ValidationException.class, () -> consentService.getConsent("consentId"));
	}

	@Test
	void testRevokeConsentNegative(){
		assertThrows(ValidationException.class, () -> consentService.revokeConsent("consentId", "token"));
	}

	@Test
	void testRevokeConsent(){
		when(customerConsentRepository.findByConsentId(anyString())).thenReturn(new CustomerConsent());
		when(authorizationService.findByToken(anyString(), any())).thenReturn(Mockito.mock(OAuth2Authorization.class));
		assertDoesNotThrow(() ->consentService.revokeConsent("consentId", "token"));
	}

}
