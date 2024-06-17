package com.mastercard.fdx.mock.oauth2.server.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentGrant {

	private String id;
	private String status;
	private Timestamp createdTime;
	private Timestamp expirationTime;
	private String durationType;
	private String lookbackPeriod;
	private String resources;
	private List parties;
}
