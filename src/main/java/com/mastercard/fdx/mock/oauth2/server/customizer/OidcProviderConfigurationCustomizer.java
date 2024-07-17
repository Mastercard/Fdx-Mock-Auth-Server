package com.mastercard.fdx.mock.oauth2.server.customizer;

import static com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant.*;
import com.mastercard.fdx.mock.oauth2.server.common.FdxDataCluster;
import com.mastercard.fdx.mock.oauth2.server.service.AuthServerService;
import org.springframework.security.oauth2.server.authorization.oidc.OidcProviderConfiguration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

public class OidcProviderConfigurationCustomizer implements Consumer<OidcProviderConfiguration.Builder> {

    private final String baseSecureUrl;

    public OidcProviderConfigurationCustomizer(String baseSecureUrl) {
        this.baseSecureUrl = baseSecureUrl;
    }

    /**
     * Below method provides the customization for well-known configuration.
     * For e.g., custom register path /fdx/v6/register, adding scopes or specifying algorithms supported.
     * @param builder the input argument
     */
    @Override
    public void accept(OidcProviderConfiguration.Builder builder) {
        builder.registrationEndpoint(asUrl(baseSecureUrl, CLIENT_REGISTER_URI))
                .pushAuthorizeRequestEndpoint(asUrl(baseSecureUrl, OAUTH2_PAR_URI))
                .tokenEndpoint(asUrl(baseSecureUrl, OAUTH2_TOKEN_URI))
                .userInfoEndpoint(asUrl(baseSecureUrl, DEFAULT_OIDC_USER_INFO_ENDPOINT_URI))
                .tokenIntrospectionEndpoint(asUrl(baseSecureUrl, OAUTH2_INTROSPECT_URI))
                .tokenRevocationEndpoint(asUrl(baseSecureUrl, DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI))
                .scopes(scopes -> {
                    scopes.addAll( Arrays.stream(FdxDataCluster.values()).map(FdxDataCluster::getScope).toList());
                    scopes.add(AuthServerService.CLIENT_SCOPE_CREATE);
                    scopes.add(AuthServerService.CLIENT_SCOPE_READ);
                })
                .idTokenSigningAlgorithms(signingAlgorithms -> {
                    signingAlgorithms.clear();
                    signingAlgorithms.add(SignatureAlgorithm.PS256.getName());
                });
    }

    private static String asUrl(String baseUri, String endpoint) {
        return UriComponentsBuilder.fromUriString(baseUri).path(endpoint).build().toUriString();
    }
}