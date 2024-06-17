package com.mastercard.fdx.mock.oauth2.server.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FdxDataCluster {
	ACCOUNT_BASIC("fdx:accountbasic:read"),
	ACCOUNT_DETAILED("fdx:accountdetailed:read"),
	ACCOUNT_PAYMENTS("fdx:accountpayments:read"),
	BILLS("fdx:bills:read"),
	CUSTOMER_CONTACT("fdx:customercontact:read"),
	CUSTOMER_PERSONAL("fdx:customerpersonal:read"),
	IMAGES("fdx:images:read"),
	INVESTMENTS("fdx:investments:read"),
	PAYMENT_SUPPORT("fdx:paymentsupport:read"),
	REWARDS("fdx:rewards:read"),
	STATEMENTS("fdx:statements:read"),
	TAX("fdx:tax:read"),
	TRANSACTIONS("fdx:transactions:read");

	String scope;
}
