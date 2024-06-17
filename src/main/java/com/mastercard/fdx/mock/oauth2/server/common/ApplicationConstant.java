package com.mastercard.fdx.mock.oauth2.server.common;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;

import java.util.UUID;

public class ApplicationConstant {

	private ApplicationConstant() {}

	public static final String SUCCESS = "SUCCESS";

	// HttpHeaders that aren't covered by HttpHeaders
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String SSL_CLIENT_VERIFY = "ssl-client-verify";

	// OAuth2 Parameter Names that aren't covered by Spring Security
	public static final String OAUTH2_PARAM_REQUEST = "request";
	public static final String OAUTH2_PARAM_RESPONSE_MODE = "response_mode";
	public static final String OAUTH2_PARAM_REQUEST_URI = "request_uri";

	// Token Introspection Params
	public static final String INTROSPECT_PARAM_TOKEN = "token";
	public static final String INTROSPECT_PARAM_TOKEN_TYPE_HINT = "token_type_hint";
	public static final String INTROSPECT_PARAM_CLIENT_ID = "client_id";

	//Consent Claims
	public static final String CONSENT_ID = "consent_id";
	public static final String GRANT_ID = "grant_id";
	public static final String FDX_CONSENT_ID = "fdxConsentId";
	public static final String ACCOUNT_ID = "account_id";
	public static final String SHARING_EXPIRES_AT = "sharing_expires_at";


	// PAR CLAIMS
	public static final String PAR_CLAIMS = "claims";
	public static final String PAR_CLAIMS_CONSENT_ID = "consent_id";
	public static final String PAR_CLAIMS_SHARING_DURATION = "sharing_duration";

	// OAuth2 Client Assertion Types
	public static final String OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

	// RemoteJWKSet Timeout Properties
	public static final String REMOTE_JWK_SET_DEFAULT_HTTP_CONNECTION_TIMEOUT_PROP
			= RemoteJWKSet.class.getName() + ".defaultHttpConnectTimeout";

	public static final String REMOTE_JWK_SET_DEFAULT_HTTP_READ_TIMEOUT_PROP
			= RemoteJWKSet.class.getName() + ".defaultHttpReadTimeout";

	public static final String PS_KID = "99bc33ee-a04d-4a89-8ae0-5debd2145aab";

	public static final String RS_KID = "372f1117-21ec-41db-a5ab-3bebdd5faac4";

	public static final String DEFAULT_AUTHORIZATION_ENDPOINT_URI = "/oauth2/authorize";
	public static final String CLIENT_REGISTER_URI = "/fdx/v6/register";
	public static final String OAUTH2_PAR_URI = "/oauth2/par";
	public static final String DEFAULT_OIDC_USER_INFO_ENDPOINT_URI = "/userinfo";
	public static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = "/revoke";
	public static final String OAUTH2_TOKEN_URI = "/oauth2/v1/token";
	public static final String OAUTH2_INTROSPECT_URI = "/oauth2/introspect";

}
