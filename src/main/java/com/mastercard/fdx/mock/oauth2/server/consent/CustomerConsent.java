package com.mastercard.fdx.mock.oauth2.server.consent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerConsent {

    private String consentId;
    private Timestamp createdDate;
    private Timestamp endDate;
    private String authCode;
    private String userId;
    private String accountIds;
    private String status;

}
