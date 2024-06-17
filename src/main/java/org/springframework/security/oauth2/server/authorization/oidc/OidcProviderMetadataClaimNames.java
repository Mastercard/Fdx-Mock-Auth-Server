package org.springframework.security.oauth2.server.authorization.oidc;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationServerMetadataClaimNames;

/**
 * The names of the "claims" defined by OpenID Connect Discovery 1.0 that can be returned
 * in the OpenID Provider Configuration Response.
 *
 * @see OAuth2AuthorizationServerMetadataClaimNames
 * @see <a target="_blank" href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">3. OpenID Provider Metadata</a>
 */
public final class OidcProviderMetadataClaimNames extends OAuth2AuthorizationServerMetadataClaimNames {

    /**
     * {@code subject_types_supported} - the Subject Identifier types supported
     */
    public static final String SUBJECT_TYPES_SUPPORTED = "subject_types_supported";

    /**
     * {@code id_token_signing_alg_values_supported} - the {@link JwsAlgorithm JWS} signing algorithms supported for the {@link OidcIdToken ID Token}
     */
    public static final String ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED = "id_token_signing_alg_values_supported";

    /**
     * {@code userinfo_endpoint} - the {@code URL} of the OpenID Connect 1.0 UserInfo Endpoint
     * @since 0.2.2
     */
    public static final String USER_INFO_ENDPOINT = "userinfo_endpoint";

    public static final String REGISTRATION_ENDPOINT = "registration_endpoint";
    public static final String PUSHED_AUTHORIZATION_REQUEST_ENDPOINT = "pushed_authorization_request_endpoint";

    public static final String END_SESSION_ENDPOINT = "end_session_endpoint";

    private OidcProviderMetadataClaimNames() {
    }

}
