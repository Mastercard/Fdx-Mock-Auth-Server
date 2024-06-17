package com.mastercard.fdx.mock.oauth2.server.consent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Party {

	private String name;
	private String type;
	private String homeUri;
	private String logoUri;
	private String registry;
	private String registeredEntityName;
	private String registeredEntityId;
}
