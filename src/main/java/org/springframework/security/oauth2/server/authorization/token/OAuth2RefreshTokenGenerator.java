/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.server.authorization.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static com.mastercard.fdx.mock.oauth2.server.utils.OAuth2AuthorizationRequestUtils.*;

@Slf4j
public final class OAuth2RefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {
    private final StringKeyGenerator refreshTokenGenerator =
            new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    @Nullable
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        } else if (isPublicClientForAuthorizationCodeGrant(context)) {
            return null;
        } else {
            Instant issuedAt = Instant.now();
            Instant expiresAt = getExpiresAt(context, issuedAt);
            return new OAuth2RefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
        }
    }

    // If AuthorizationRequest contains SharingDuration then use this for determining expiresAt
    private Instant getExpiresAt(OAuth2TokenContext context, Instant issuedAt) {
        OAuth2AuthorizationRequest authorizationRequest = getOAuth2AuthorizationRequest(context);
        if (authorizationRequest != null) {
            long sharingDuration = getSharingDuration(authorizationRequest);
            if (sharingDuration != -1) {
                OAuth2Authorization.Token<OAuth2RefreshToken> currentRefreshToken = context.getAuthorization().getRefreshToken();
                if (currentRefreshToken != null)
                    // reused the previous determined refreshToken expiresAt as it represents ConsentExpiry
                    return currentRefreshToken.getToken().getExpiresAt();
                else
                    return issuedAt.plus(adjustSharingDuration(sharingDuration), ChronoUnit.SECONDS);
            }
        }

        // Calculate based on the actual system RefreshToken TTL
        return issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());
    }

    private static boolean isPublicClientForAuthorizationCodeGrant(OAuth2TokenContext context) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())) {
            Object var2 = context.getAuthorizationGrant().getPrincipal();
            if (var2 instanceof OAuth2ClientAuthenticationToken) {
                OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken)var2;
                return clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE);
            }
        }

        return false;
    }

}
