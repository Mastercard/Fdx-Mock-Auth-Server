package org.springframework.security.oauth2.server.authorization.oidc;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.server.authorization.AbstractOAuth2AuthorizationServerMetadata;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A representation of an OpenID Provider Configuration Response,
 * which is returned from an Issuer's Discovery Endpoint,
 * and contains a set of claims about the OpenID Provider's configuration.
 * The claims are defined by the OpenID Connect Discovery 1.0 specification.
 *
 * @see AbstractOAuth2AuthorizationServerMetadata
 * @see OidcProviderMetadataClaimAccessor
 * @see <a target="_blank" href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse">4.2. OpenID Provider Configuration Response</a>
 */
public final class OidcProviderConfiguration extends AbstractOAuth2AuthorizationServerMetadata
        implements OidcProviderMetadataClaimAccessor {

    private OidcProviderConfiguration(Map<String, Object> claims) {
        super(claims);
    }

    /**
     * Constructs a new {@link Builder} with empty claims.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs a new {@link Builder} with the provided claims.
     *
     * @param claims the claims to initialize the builder
     */
    public static Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return new Builder()
                .claims(c -> c.putAll(claims));
    }

    /**
     * Helps configure an {@link OidcProviderConfiguration}.
     */
    public static class Builder extends AbstractBuilder<OidcProviderConfiguration, Builder> {

        private Builder() {
        }

        /**
         * Add this Subject Type to the collection of {@code subject_types_supported} in the resulting
         * {@link OidcProviderConfiguration}, REQUIRED.
         *
         * @param subjectType the Subject Type that the OpenID Provider supports
         * @return the {@link Builder} for further configuration
         */
        public Builder subjectType(String subjectType) {
            addClaimToClaimList(OidcProviderMetadataClaimNames.SUBJECT_TYPES_SUPPORTED, subjectType);
            return this;
        }

        /**
         * A {@code Consumer} of the Subject Types(s) allowing the ability to add, replace, or remove.
         *
         * @param subjectTypesConsumer a {@code Consumer} of the Subject Types(s)
         * @return the {@link Builder} for further configuration
         */
        public Builder subjectTypes(Consumer<List<String>> subjectTypesConsumer) {
            acceptClaimValues(OidcProviderMetadataClaimNames.SUBJECT_TYPES_SUPPORTED, subjectTypesConsumer);
            return this;
        }

        /**
         * Add this {@link JwsAlgorithm JWS} signing algorithm to the collection of {@code id_token_signing_alg_values_supported}
         * in the resulting {@link OidcProviderConfiguration}, REQUIRED.
         *
         * @param signingAlgorithm the {@link JwsAlgorithm JWS} signing algorithm supported for the {@link OidcIdToken ID Token}
         * @return the {@link Builder} for further configuration
         */
        public Builder idTokenSigningAlgorithm(String signingAlgorithm) {
            addClaimToClaimList(OidcProviderMetadataClaimNames.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED, signingAlgorithm);
            return this;
        }

        /**
         * A {@code Consumer} of the {@link JwsAlgorithm JWS} signing algorithms for the {@link OidcIdToken ID Token}
         * allowing the ability to add, replace, or remove.
         *
         * @param signingAlgorithmsConsumer a {@code Consumer} of the {@link JwsAlgorithm JWS} signing algorithms for the {@link OidcIdToken ID Token}
         * @return the {@link Builder} for further configuration
         */
        public Builder idTokenSigningAlgorithms(Consumer<List<String>> signingAlgorithmsConsumer) {
            acceptClaimValues(OidcProviderMetadataClaimNames.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED, signingAlgorithmsConsumer);
            return this;
        }

        /**
         * Use this {@code userinfo_endpoint} in the resulting {@link OidcProviderConfiguration}, OPTIONAL.
         *
         * @param userInfoEndpoint the {@code URL} of the OpenID Connect 1.0 UserInfo Endpoint
         * @return the {@link Builder} for further configuration
         * @since 0.2.2
         */
        public Builder userInfoEndpoint(String userInfoEndpoint) {
            return claim(OidcProviderMetadataClaimNames.USER_INFO_ENDPOINT, userInfoEndpoint);
        }

        public Builder registrationEndpoint(String registrationEndpoint) {
            return claim(OidcProviderMetadataClaimNames.REGISTRATION_ENDPOINT, registrationEndpoint);
        }

        public Builder pushAuthorizeRequestEndpoint(String parEndpoint) {
            return claim(OidcProviderMetadataClaimNames.PUSHED_AUTHORIZATION_REQUEST_ENDPOINT, parEndpoint);
        }

        /**
         * Use this {@code end_session_endpoint} in the resulting {@link OidcProviderConfiguration}, OPTIONAL.
         *
         * @param endSessionEndpoint the {@code URL} of the OpenID Connect 1.0 End Session Endpoint
         * @return the {@link Builder} for further configuration
         * @since 1.1
         */
        public Builder endSessionEndpoint(String endSessionEndpoint) {
            return claim(OidcProviderMetadataClaimNames.END_SESSION_ENDPOINT, endSessionEndpoint);
        }

        /**
         * Validate the claims and build the {@link OidcProviderConfiguration}.
         * <p>
         * The following claims are REQUIRED:
         * {@code issuer}, {@code authorization_endpoint}, {@code token_endpoint}, {@code jwks_uri},
         * {@code response_types_supported}, {@code subject_types_supported} and
         * {@code id_token_signing_alg_values_supported}.
         *
         * @return the {@link OidcProviderConfiguration}
         */
        @Override
        public OidcProviderConfiguration build() {
            validate();
            return new OidcProviderConfiguration(getClaims());
        }

        @Override
        protected void validate() {
            super.validate();
            Assert.notNull(getClaims().get(OidcProviderMetadataClaimNames.JWKS_URI), "jwksUri cannot be null");
            Assert.notNull(getClaims().get(OidcProviderMetadataClaimNames.SUBJECT_TYPES_SUPPORTED), "subjectTypes cannot be null");
            Assert.isInstanceOf(List.class, getClaims().get(OidcProviderMetadataClaimNames.SUBJECT_TYPES_SUPPORTED), "subjectTypes must be of type List");
            Assert.notEmpty((List<?>) getClaims().get(OidcProviderMetadataClaimNames.SUBJECT_TYPES_SUPPORTED), "subjectTypes cannot be empty");
            Assert.notNull(getClaims().get(OidcProviderMetadataClaimNames.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED), "idTokenSigningAlgorithms cannot be null");
            Assert.isInstanceOf(List.class, getClaims().get(OidcProviderMetadataClaimNames.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED), "idTokenSigningAlgorithms must be of type List");
            Assert.notEmpty((List<?>) getClaims().get(OidcProviderMetadataClaimNames.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED), "idTokenSigningAlgorithms cannot be empty");
            if (getClaims().get(OidcProviderMetadataClaimNames.USER_INFO_ENDPOINT) != null) {
                validateURL(getClaims().get(OidcProviderMetadataClaimNames.USER_INFO_ENDPOINT), "userInfoEndpoint must be a valid URL");
            }
            if (getClaims().get(OidcProviderMetadataClaimNames.END_SESSION_ENDPOINT) != null) {
                validateURL(getClaims().get(OidcProviderMetadataClaimNames.END_SESSION_ENDPOINT), "endSessionEndpoint must be a valid URL");
            }
        }

        @SuppressWarnings("unchecked")
        private void addClaimToClaimList(String name, String value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            getClaims().computeIfAbsent(name, k -> new LinkedList<String>());
            ((List<String>) getClaims().get(name)).add(value);
        }

        @SuppressWarnings("unchecked")
        private void acceptClaimValues(String name, Consumer<List<String>> valuesConsumer) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(valuesConsumer, "valuesConsumer cannot be null");
            getClaims().computeIfAbsent(name, k -> new LinkedList<String>());
            List<String> values = (List<String>) getClaims().get(name);
            valuesConsumer.accept(values);
        }

    }
}
