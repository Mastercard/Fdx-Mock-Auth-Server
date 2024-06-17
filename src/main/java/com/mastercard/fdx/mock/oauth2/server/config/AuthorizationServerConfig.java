package com.mastercard.fdx.mock.oauth2.server.config;

import com.github.openjson.JSONException;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsent;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsentRepository;
import com.mastercard.fdx.mock.oauth2.server.consent.JdbcCustomerConsentRepository;
import com.mastercard.fdx.mock.oauth2.server.customizer.OidcProviderConfigurationCustomizer;
import com.mastercard.fdx.mock.oauth2.server.filter.InjectAuthorizeParamsFilter;
import com.mastercard.fdx.mock.oauth2.server.filter.PARInjectAuthorizeParamsFilter;
import com.mastercard.fdx.mock.oauth2.server.par.JdbcPushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.CommonUtils;
import com.mastercard.fdx.mock.oauth2.server.utils.Jwks;
import com.mastercard.fdx.mock.oauth2.server.utils.JwksUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2ConfigurerUtils;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class AuthorizationServerConfig {

	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Autowired
	ApplicationContext appCtx;

	@Autowired
	ApplicationProperties props;

	private CustomerConsentRepository customerConsentRepository;

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

		System.setProperty(ApplicationConstant.REMOTE_JWK_SET_DEFAULT_HTTP_CONNECTION_TIMEOUT_PROP,
				Integer.toString(props.getRemoteJWKSetDefaultHttpConnectTimeout()));
		System.setProperty(ApplicationConstant.REMOTE_JWK_SET_DEFAULT_HTTP_READ_TIMEOUT_PROP,
				Integer.toString(props.getRemoteJWKSetDefaultHttpReadTimeout()));

		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				new OAuth2AuthorizationServerConfigurer();

		OAuth2AuthorizationCodeRequestAuthenticationProvider authorizationCodeRequestAuthenticationProvider =
				new OAuth2AuthorizationCodeRequestAuthenticationProvider(
						OAuth2ConfigurerUtils.getRegisteredClientRepository(http),
						OAuth2ConfigurerUtils.getAuthorizationService(http),
						OAuth2ConfigurerUtils.getAuthorizationConsentService(http));

		authorizationServerConfigurer
				.oidc(oidc -> {

					oidc.clientRegistrationEndpoint(Customizer.withDefaults());

					oidc.providerConfigurationEndpoint(providerConfigurationEndpoint ->
							providerConfigurationEndpoint
									.providerConfigurationCustomizer(new OidcProviderConfigurationCustomizer(props.getAuthServerBaseUrl())));

					PARInjectAuthorizeParamsFilter filter = (PARInjectAuthorizeParamsFilter) appCtx.getBean("PARInjectAuthorizeParamsFilter");
					http.addFilterBefore(filter, AbstractPreAuthenticatedProcessingFilter.class);

					InjectAuthorizeParamsFilter filter1 = (InjectAuthorizeParamsFilter) appCtx.getBean("InjectAuthorizeParamsFilter");
					http.addFilterBefore(filter1, PARInjectAuthorizeParamsFilter.class);

				}).authorizationEndpoint(authorizationEndpoint -> {
					authorizationEndpoint.authorizationResponseHandler(this::sendAuthorizationResponse);
					authorizationEndpoint.consentPage(props.getConsentUri());
					authorizationEndpoint.authenticationProvider(authorizationCodeRequestAuthenticationProvider);
				});

		RequestMatcher endpointsMatcher = authorizationServerConfigurer
				.getEndpointsMatcher();

		http
				.securityMatcher(endpointsMatcher)
				.authorizeRequests(authorizeRequests ->
						authorizeRequests.anyRequest().authenticated()
				)
				.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)     // Needed to enable the BearerTokenAuthenticationFilter
				.apply(authorizationServerConfigurer);

		customerConsentRepository = (CustomerConsentRepository) appCtx.getBean("customerConsentRepository");

		return http.formLogin(Customizer.withDefaults()).build();
	}


	private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
				(OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(authorizationCodeRequestAuthentication.getRedirectUri())
				.queryParam(OAuth2ParameterNames.CODE, authorizationCodeRequestAuthentication.getAuthorizationCode().getTokenValue());
		if (StringUtils.hasText(authorizationCodeRequestAuthentication.getState())) {
			uriBuilder.queryParam(OAuth2ParameterNames.STATE, authorizationCodeRequestAuthentication.getState());
		}

		saveCustomerConsentDetails(request, authentication, authorizationCodeRequestAuthentication);
		this.redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString());
	}

	private void saveCustomerConsentDetails(HttpServletRequest request, Authentication authentication, OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication) {
		String consentId = request.getParameterMap().get("consentId")[0];
		String endDate = request.getParameterMap().get("endDate")[0];
		String accountIds = request.getParameterMap().get("accountIds")[0];

		CustomerConsent customerConsent = new CustomerConsent(
				consentId,
				new Timestamp(System.currentTimeMillis()),
				new Timestamp(Long.parseLong(endDate)),
				authorizationCodeRequestAuthentication.getAuthorizationCode().getTokenValue(),
				authentication.getName(),
				accountIds,
				"Active");

		customerConsentRepository.save(customerConsent);
	}

	@Bean
	public CustomerConsentRepository customerConsentRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcCustomerConsentRepository(jdbcTemplate);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
		// Save registered client in db as if in-memory
		return new JdbcRegisteredClientRepository(jdbcTemplate);
	}

	@Bean
	@SuppressWarnings("unused")
	public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public PushAuthorizationRequestRepository pushAuthorizationRequestRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcPushAuthorizationRequestRepository(jdbcTemplate);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() throws JSONException, JOSEException, URISyntaxException, IOException {
		String pk = CommonUtils.getFileContent("dh","/dhpk.txt");

		JWK key = Jwks.loadKeyFromPem(pk);
		if (key == null) {
			throw new JOSEException("Failed to load Jwks");
		}
		JWK psjwk = JwksUtil.generateJwk(KeyUse.SIGNATURE, JWSAlgorithm.PS256, key, ApplicationConstant.PS_KID);
		JWK rsjwk = JwksUtil.generateJwk(KeyUse.SIGNATURE, JWSAlgorithm.RS256, key, ApplicationConstant.RS_KID);
		JWKSet jwkSet = new JWKSet(List.of(psjwk, rsjwk));
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	/*@Bean for creating DR jwks
	public JWKSource<SecurityContext> jwkSource() throws JSONException, JOSEException, URISyntaxException, IOException {
		URL resource = this.getClass().getClassLoader().getResource("dr/drpk.txt");
		String pk = new String(Files.readAllBytes( new File(resource.toURI()).toPath()), Charset.defaultCharset());
		JWK key = Jwks.loadKeyFromPem(pk);
		if (key == null) {
			throw new JOSEException("Failed to load Jwks");
		}
		List<JWK> jwks = new ArrayList<>();
		jwks.add(JwksUtil.generateJwk(KeyUse.SIGNATURE, JWSAlgorithm.PS256, key, "edb748f7-b745-4b3f-a966-34f7c257a897"));
		//JSON Web Key Use : enc, Encryption
		jwks.add(JwksUtil.generateJwk(KeyUse.ENCRYPTION, JWEAlgorithm.RSA_OAEP_256, key, "1fdb0c4b-e6fe-494d-943f-49135a8bb8ca"));
		jwks.add(JwksUtil.generateJwk(KeyUse.ENCRYPTION, JWEAlgorithm.RSA_OAEP, key, "a6f49128-cb52-4531-a4da-b014800d315e"));

		JWKSet jwkSet = new JWKSet(jwks);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}*/

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
		return context -> {
			context.getJwsHeader().headers(list -> {
				list.clear();
				Map<String, Object> headers = new HashMap<>(
						JwsHeader.with(SignatureAlgorithm.PS256).build().getHeaders());
				headers.put("kid", ApplicationConstant.PS_KID);
				headers.put("typ", "at+jwt");
				list.putAll(headers);
			});
			addConsentClaims(context);
		};
	}

	private void addConsentClaims(JwtEncodingContext context) {
		if ((context.getAuthorization() != null) &&
				(context.getAuthorization().getToken(OAuth2AuthorizationCode.class) != null)) {

			String authCode = context.getAuthorization().getToken(OAuth2AuthorizationCode.class).getToken().getTokenValue();
			CustomerConsent customerConsent = customerConsentRepository.findByAuthCode(authCode);
			if (customerConsent != null) {
				// Add to all TokenTypes (Access, Id and Refresh)
				context.getClaims().claim(ApplicationConstant.FDX_CONSENT_ID, customerConsent.getConsentId());
				context.getClaims().claim(ApplicationConstant.SHARING_EXPIRES_AT,
						(customerConsent.getEndDate().getTime() / 1000l));
				context.getClaims().claim(ApplicationConstant.ACCOUNT_ID, customerConsent.getAccountIds().split(","));
			}
		}
	}

	@Bean
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		// copied code from OAuth2AuthorizationServerConfiguration.jwtDecoder method and modified to verify type
		Set<JWSAlgorithm> jwsAlgs = new HashSet<>();
		jwsAlgs.addAll(JWSAlgorithm.Family.RSA);
		jwsAlgs.addAll(JWSAlgorithm.Family.EC);
		jwsAlgs.addAll(JWSAlgorithm.Family.HMAC_SHA);
		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
		JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(jwsAlgs, jwkSource);
		jwtProcessor.setJWSKeySelector(jwsKeySelector);
		// added to verify type at+Jwt in header
		jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, new JOSEObjectType("at+jwt")));
		// Override the default Nimbus claims set verifier as NimbusJwtDecoder handles it instead
		jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
		});
		return new NimbusJwtDecoder(jwtProcessor);
	}

	@Bean
	public AuthorizationServerSettings providerSettings() {
		return AuthorizationServerSettings.builder()
				.issuer(props.getAuthServerBaseUrl())
				.jwkSetEndpoint(props.getAuthServerJwksUriPath())
				.build();
	}

	@Bean
	public RestTemplate setupRestTemplate() {
		return new RestTemplate();
	}
}
