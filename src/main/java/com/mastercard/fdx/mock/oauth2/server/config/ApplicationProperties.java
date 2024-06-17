package com.mastercard.fdx.mock.oauth2.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@Getter
@Setter
public class ApplicationProperties {

	//DB Configurations
	@Value("${aurora.datasource.driver.class.name}")
	private String driverClassName;

	@Value("${aurora.datasource.jdbcUrl}")
	private String jdbcUrl;

	@Value("${aurora.datasource.username.path}")
	private String usernamePath;

	@Value("${aurora.datasource.password.path}")
	private String passwordPath;

	@Value("${aurora.datasource.dialect}")
	private String dialect;

	@Value("${hikari.connectionTimeout}")
	private int connectionTimeout;

	@Value("${hikari.prepStmtCacheSize}")
	private int prepStmtCacheSize;

	@Value("${hikari.prepStmtCacheSqlLimit}")
	private int prepStmtCacheSqlLimit;

	@Value("${hikari.idleTimeout}")
	private int idleTimeout;

	@Value("${hikari.useServerPrepStmts}")
	private boolean useServerPrepStmts;

	@Value("${hikari.cachePrepStmts}")
	private boolean cachePrepStmts;

	@Value("${RemoteJWKSet.defaultHttpConnectTimeout}")
	private int remoteJWKSetDefaultHttpConnectTimeout;

	@Value("${RemoteJWKSet.defaultHttpReadTimeout}")
	private int remoteJWKSetDefaultHttpReadTimeout;

	@Value("${mock.auth.server.dh.clientId}")
	private String dhClientId;

	@Value("${mock.auth.server.dh.clientSecret}")
	private String dhClientSecret;

	@Value("${mock.auth.server.baseurl}")
	private String authServerBaseUrl;

	@Value("${mock.auth.server.baseSecureUrl}")
	private String authServerBaseSecureUrl;

	@Value("${mock.res.server.baseurl}")
	private String mockResServerBaseUrl;

	@Value("${mock.res.server.userurl}")
	private String mockResServerUserUrl;

	@Value("${mock.auth.server.consent_uri}")
	private String consentUri;

	@Value("${server.port}")
	private String serverPort;
	
	@Value("${mock.res.server.auth.code}")
	private String resourceServerAuthCode;

	@Value("${mock.auth.server.jwks_uri.path}")
	private String authServerJwksUriPath;

	public String getLocalServerBaseUri() {
		return "http://localhost:" + getServerPort();
	}
}
