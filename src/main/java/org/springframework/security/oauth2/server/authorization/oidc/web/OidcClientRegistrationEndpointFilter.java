//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.security.oauth2.server.authorization.oidc.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.http.converter.OidcClientRegistrationHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcClientRegistrationAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public final class OidcClientRegistrationEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_OIDC_CLIENT_REGISTRATION_ENDPOINT_URI = "/connect/register";
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher clientRegistrationEndpointMatcher;
    private final HttpMessageConverter<OidcClientRegistration> clientRegistrationHttpMessageConverter;
    private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter;
    private AuthenticationConverter authenticationConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;

    public OidcClientRegistrationEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, "/connect/register");
    }

    public OidcClientRegistrationEndpointFilter(AuthenticationManager authenticationManager, String clientRegistrationEndpointUri) {
        this.clientRegistrationHttpMessageConverter = new OidcClientRegistrationHttpMessageConverter();
        this.errorHttpResponseConverter = new OAuth2ErrorHttpMessageConverter();
        this.authenticationConverter = new OidcClientRegistrationAuthenticationConverter();
        this.authenticationSuccessHandler = this::sendClientRegistrationResponse;
        this.authenticationFailureHandler = this::sendErrorResponse;
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(clientRegistrationEndpointUri, "clientRegistrationEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.clientRegistrationEndpointMatcher = new OrRequestMatcher(new RequestMatcher[]{new AntPathRequestMatcher(clientRegistrationEndpointUri, HttpMethod.POST.name()), createClientConfigurationMatcher(clientRegistrationEndpointUri)});
    }

    private static RequestMatcher createClientConfigurationMatcher(String clientRegistrationEndpointUri) {
        RequestMatcher clientConfigurationGetMatcher = new AntPathRequestMatcher(clientRegistrationEndpointUri, HttpMethod.GET.name());
        RequestMatcher clientIdMatcher = (request) -> {
            String clientId = request.getParameter("client_id");
            return StringUtils.hasText(clientId);
        };
        return new AndRequestMatcher(new RequestMatcher[]{clientConfigurationGetMatcher, clientIdMatcher});
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!this.clientRegistrationEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                Authentication clientRegistrationAuthentication = this.authenticationConverter.convert(request);
                Authentication clientRegistrationAuthenticationResult = this.authenticationManager.authenticate(clientRegistrationAuthentication);
                this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, clientRegistrationAuthenticationResult);
            } catch (OAuth2AuthenticationException var10) {
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace(LogMessage.format("Client registration request failed: %s", var10.getError()), var10);
                }

                this.authenticationFailureHandler.onAuthenticationFailure(request, response, var10);
            } catch (Exception var11) {
                OAuth2Error error = new OAuth2Error("invalid_request", "OpenID Connect 1.0 Client Registration Error: " + var11.getMessage(), "https://openid.net/specs/openid-connect-registration-1_0.html#RegistrationError");
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace(error.getDescription(), var11);
                }

                this.authenticationFailureHandler.onAuthenticationFailure(request, response, new OAuth2AuthenticationException(error));
            } finally {
                SecurityContextHolder.clearContext();
            }

        }
    }

    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
        this.authenticationConverter = authenticationConverter;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    private void sendClientRegistrationResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OidcClientRegistration clientRegistration = ((OidcClientRegistrationAuthenticationToken)authentication).getClientRegistration();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        if (HttpMethod.POST.name().equals(request.getMethod())) {
            httpResponse.setStatusCode(HttpStatus.CREATED);
        } else {
            httpResponse.setStatusCode(HttpStatus.OK);
        }

        this.clientRegistrationHttpMessageConverter.write(clientRegistration, (MediaType)null, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {
        OAuth2Error error = ((OAuth2AuthenticationException)authenticationException).getError();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if ("invalid_token".equals(error.getErrorCode())) {
            httpStatus = HttpStatus.UNAUTHORIZED;
        } else if ("insufficient_scope".equals(error.getErrorCode())) {
            httpStatus = HttpStatus.FORBIDDEN;
        } else if ("invalid_client".equals(error.getErrorCode())) {
            httpStatus = HttpStatus.UNAUTHORIZED;
        }

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(httpStatus);
        this.errorHttpResponseConverter.write(error, (MediaType)null, httpResponse);
    }
}
