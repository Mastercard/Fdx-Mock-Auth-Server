package org.springframework.security.oauth2.server.authorization.authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public final class OAuth2AuthorizationConsentAuthenticationProvider implements AuthenticationProvider {
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";
	private static final OAuth2TokenType STATE_TOKEN_TYPE = new OAuth2TokenType("state");
	private final Log logger = LogFactory.getLog(this.getClass());
	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2AuthorizationConsentService authorizationConsentService;
	private OAuth2TokenGenerator<OAuth2AuthorizationCode> authorizationCodeGenerator = new OAuth2AuthorizationCodeGenerator();
	private Consumer<OAuth2AuthorizationConsentAuthenticationContext> authorizationConsentCustomizer;

	public OAuth2AuthorizationConsentAuthenticationProvider(RegisteredClientRepository registeredClientRepository, OAuth2AuthorizationService authorizationService, OAuth2AuthorizationConsentService authorizationConsentService) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(authorizationConsentService, "authorizationConsentService cannot be null");
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;
		this.authorizationConsentService = authorizationConsentService;
	}

	/**
	 * Below method is overwritten from the default Spring AS - OAuth2AuthorizationConsentAuthenticationProvider
	 * after successful consent authorization code is generated and stored. For failure, handles the error case.
	 * @param authentication
	 * @return
	 * @throws AuthenticationException
	 */
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication instanceof OAuth2DeviceAuthorizationConsentAuthenticationToken) {
			return null;
		} else {
			OAuth2AuthorizationConsentAuthenticationToken authorizationConsentAuthentication = (OAuth2AuthorizationConsentAuthenticationToken)authentication;
			OAuth2Authorization authorization = this.authorizationService.findByToken(authorizationConsentAuthentication.getState(), STATE_TOKEN_TYPE);
			if (authorization == null) {
				throwError((String)"invalid_request", "state", authorizationConsentAuthentication, (RegisteredClient)null, (OAuth2AuthorizationRequest)null);
			}

			if (this.logger.isTraceEnabled()) {
				this.logger.trace("Retrieved authorization with authorization consent state");
			}

			Authentication principal = (Authentication)authorizationConsentAuthentication.getPrincipal();
			if (!isPrincipalAuthenticated(principal) || !principal.getName().equals(authorization.getPrincipalName())) {
				throwError((String)"invalid_request", "state", authorizationConsentAuthentication, (RegisteredClient)null, (OAuth2AuthorizationRequest)null);
			}

			RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(authorizationConsentAuthentication.getClientId());
			if (registeredClient == null || !registeredClient.getId().equals(authorization.getRegisteredClientId())) {
				throwError((String)"invalid_request", "client_id", authorizationConsentAuthentication, registeredClient, (OAuth2AuthorizationRequest)null);
			}

			if (this.logger.isTraceEnabled()) {
				this.logger.trace("Retrieved registered client");
			}

			OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest)authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
			Set<String> requestedScopes = authorizationRequest.getScopes();
			Set<String> authorizedScopes = new HashSet(authorizationConsentAuthentication.getScopes());
			if (!requestedScopes.containsAll(authorizedScopes)) {
				throwError("invalid_scope", "scope", authorizationConsentAuthentication, registeredClient, authorizationRequest);
			}

			if(((OAuth2AuthorizationConsentAuthenticationToken) authentication).getAdditionalParameters().containsValue("user_cancelled_consent")){
				OAuth2Error error = new OAuth2Error("access_denied", "User cancelled consent process", "");
				throwError(error, "", authorizationConsentAuthentication, registeredClient, authorizationRequest);
			}

			if (this.logger.isTraceEnabled()) {
				this.logger.trace("Validated authorization consent request parameters");
			}

			OAuth2AuthorizationConsent currentAuthorizationConsent = this.authorizationConsentService.findById(authorization.getRegisteredClientId(), authorization.getPrincipalName());
			Set<String> currentAuthorizedScopes = currentAuthorizationConsent != null ? currentAuthorizationConsent.getScopes() : Collections.emptySet();
			if (!currentAuthorizedScopes.isEmpty()) {
				Iterator var11 = requestedScopes.iterator();

				while(var11.hasNext()) {
					String requestedScope = (String)var11.next();
					if (currentAuthorizedScopes.contains(requestedScope)) {
						authorizedScopes.add(requestedScope);
					}
				}
			}

			if (!authorizedScopes.isEmpty() && requestedScopes.contains("openid")) {
				authorizedScopes.add("openid");
			}

			OAuth2AuthorizationConsent.Builder authorizationConsentBuilder;
			if (currentAuthorizationConsent != null) {
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Retrieved existing authorization consent");
				}

				authorizationConsentBuilder = OAuth2AuthorizationConsent.from(currentAuthorizationConsent);
			} else {
				authorizationConsentBuilder = OAuth2AuthorizationConsent.withId(authorization.getRegisteredClientId(), authorization.getPrincipalName());
			}

			Objects.requireNonNull(authorizationConsentBuilder);
			authorizedScopes.forEach(authorizationConsentBuilder::scope);
			if (this.authorizationConsentCustomizer != null) {
				OAuth2AuthorizationConsentAuthenticationContext authorizationConsentAuthenticationContext = OAuth2AuthorizationConsentAuthenticationContext.with(authorizationConsentAuthentication).authorizationConsent(authorizationConsentBuilder).registeredClient(registeredClient).authorization(authorization).authorizationRequest(authorizationRequest).build();
				this.authorizationConsentCustomizer.accept(authorizationConsentAuthenticationContext);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Customized authorization consent");
				}
			}

			Set<GrantedAuthority> authorities = new HashSet();
			Objects.requireNonNull(authorities);
			authorizationConsentBuilder.authorities(authorities::addAll);
			if (authorities.isEmpty()) {
				if (currentAuthorizationConsent != null) {
					this.authorizationConsentService.remove(currentAuthorizationConsent);
					if (this.logger.isTraceEnabled()) {
						this.logger.trace("Revoked authorization consent");
					}
				}

				this.authorizationService.remove(authorization);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Removed authorization");
				}

				throwError("access_denied", "client_id", authorizationConsentAuthentication, registeredClient, authorizationRequest);
			}

			OAuth2AuthorizationConsent authorizationConsent = authorizationConsentBuilder.build();
			if (!authorizationConsent.equals(currentAuthorizationConsent)) {
				this.authorizationConsentService.save(authorizationConsent);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Saved authorization consent");
				}
			}

			OAuth2TokenContext tokenContext = createAuthorizationCodeTokenContext(authorizationConsentAuthentication, registeredClient, authorization, authorizedScopes);
			OAuth2AuthorizationCode authorizationCode = (OAuth2AuthorizationCode)this.authorizationCodeGenerator.generate(tokenContext);
			if (authorizationCode == null) {
				OAuth2Error error = new OAuth2Error("server_error", "The token generator failed to generate the authorization code.", "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1");
				throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, (OAuth2AuthorizationCodeRequestAuthenticationToken)null);
			} else {
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Generated authorization code");
				}

				OAuth2Authorization updatedAuthorization = OAuth2Authorization.from(authorization).authorizedScopes(authorizedScopes).token(authorizationCode).attributes((attrs) -> {
					attrs.remove("state");
				}).build();
				this.authorizationService.save(updatedAuthorization);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Saved authorization");
				}

				String redirectUri = authorizationRequest.getRedirectUri();
				if (!StringUtils.hasText(redirectUri)) {
					redirectUri = (String)registeredClient.getRedirectUris().iterator().next();
				}

				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Authenticated authorization consent request");
				}

				return new OAuth2AuthorizationCodeRequestAuthenticationToken(authorizationRequest.getAuthorizationUri(), registeredClient.getClientId(), principal, authorizationCode, redirectUri, authorizationRequest.getState(), authorizedScopes);
			}
		}
	}

	public boolean supports(Class<?> authentication) {
		return OAuth2AuthorizationConsentAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public void setAuthorizationCodeGenerator(OAuth2TokenGenerator<OAuth2AuthorizationCode> authorizationCodeGenerator) {
		Assert.notNull(authorizationCodeGenerator, "authorizationCodeGenerator cannot be null");
		this.authorizationCodeGenerator = authorizationCodeGenerator;
	}

	public void setAuthorizationConsentCustomizer(Consumer<OAuth2AuthorizationConsentAuthenticationContext> authorizationConsentCustomizer) {
		Assert.notNull(authorizationConsentCustomizer, "authorizationConsentCustomizer cannot be null");
		this.authorizationConsentCustomizer = authorizationConsentCustomizer;
	}

	private static OAuth2TokenContext createAuthorizationCodeTokenContext(OAuth2AuthorizationConsentAuthenticationToken authorizationConsentAuthentication, RegisteredClient registeredClient, OAuth2Authorization authorization, Set<String> authorizedScopes) {
		return ((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)((DefaultOAuth2TokenContext.Builder)DefaultOAuth2TokenContext.builder().registeredClient(registeredClient)).principal((Authentication)authorizationConsentAuthentication.getPrincipal())).authorization(authorization)).authorizationServerContext(
				AuthorizationServerContextHolder.getContext())).tokenType(new OAuth2TokenType("code"))).authorizedScopes(authorizedScopes)).authorizationGrantType(
				AuthorizationGrantType.AUTHORIZATION_CODE)).authorizationGrant(authorizationConsentAuthentication)).build();
	}

	private static boolean isPrincipalAuthenticated(Authentication principal) {
		return principal != null && !AnonymousAuthenticationToken.class.isAssignableFrom(principal.getClass()) && principal.isAuthenticated();
	}

	private static void throwError(String errorCode, String parameterName, OAuth2AuthorizationConsentAuthenticationToken authorizationConsentAuthentication, RegisteredClient registeredClient, OAuth2AuthorizationRequest authorizationRequest) {
		OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1");
		throwError(error, parameterName, authorizationConsentAuthentication, registeredClient, authorizationRequest);
	}

	private static void throwError(OAuth2Error error, String parameterName, OAuth2AuthorizationConsentAuthenticationToken authorizationConsentAuthentication, RegisteredClient registeredClient, OAuth2AuthorizationRequest authorizationRequest) {
		String redirectUri = resolveRedirectUri(authorizationRequest, registeredClient);
		if (error.getErrorCode().equals("invalid_request") && (parameterName.equals("client_id") || parameterName.equals("state"))) {
			redirectUri = null;
		}

		String state = authorizationRequest != null ? authorizationRequest.getState() : authorizationConsentAuthentication.getState();
		Set<String> requestedScopes = authorizationRequest != null ? authorizationRequest.getScopes() : authorizationConsentAuthentication.getScopes();
		OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthenticationResult = new OAuth2AuthorizationCodeRequestAuthenticationToken(authorizationConsentAuthentication.getAuthorizationUri(), authorizationConsentAuthentication.getClientId(), (Authentication)authorizationConsentAuthentication.getPrincipal(), redirectUri, state, requestedScopes, (Map)null);
		throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, authorizationCodeRequestAuthenticationResult);
	}

	private static String resolveRedirectUri(OAuth2AuthorizationRequest authorizationRequest, RegisteredClient registeredClient) {
		if (authorizationRequest != null && StringUtils.hasText(authorizationRequest.getRedirectUri())) {
			return authorizationRequest.getRedirectUri();
		} else {
			return registeredClient != null ? (String)registeredClient.getRedirectUris().iterator().next() : null;
		}
	}
}
