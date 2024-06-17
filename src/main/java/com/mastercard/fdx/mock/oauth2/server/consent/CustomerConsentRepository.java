package com.mastercard.fdx.mock.oauth2.server.consent;

import org.springframework.lang.Nullable;

public interface CustomerConsentRepository {

    void save(CustomerConsent customerConsent);

    @Nullable
    CustomerConsent findByConsentId(String cdrArrangementId);

    @Nullable
    CustomerConsent findByAuthCode(String authCode);

	void updateCustomerConsent(String consentId, String status);
}
