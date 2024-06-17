package com.mastercard.fdx.mock.oauth2.server.consent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountConsentResponse {

    private String customerId;

    private String consentId;

    private Timestamp endDate;
}
