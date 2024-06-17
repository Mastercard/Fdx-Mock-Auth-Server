package com.mastercard.fdx.mock.oauth2.server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiException extends Exception{
	private final HttpStatusCode code;
	public ApiException (HttpStatusCode code, String msg) {
		super(msg);
		this.code = code;
	}
}
