package com.mastercard.fdx.mock.oauth2.server.filter;

import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.helper.AuthorizeParamHelper;
import com.mastercard.fdx.mock.oauth2.server.service.PushedAuthorizationRequestService;
import com.mastercard.fdx.mock.oauth2.server.utils.ConsentResponseWrapper;
import com.mastercard.fdx.mock.oauth2.server.utils.RequestWrapper;
import com.nimbusds.oauth2.sdk.PushedAuthorizationRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component("InjectAuthorizeParamsFilter")
public class InjectAuthorizeParamsFilter extends OncePerRequestFilter {

    public static final String DEFAULT_AUTHORIZATION_ENDPOINT_URI = "/oauth2/authorize";

    private final RequestMatcher authorizationEndpointMatcher = createDefaultRequestMatcher();

    private static RequestMatcher createDefaultRequestMatcher() {
        RequestMatcher authorizationRequestGetMatcher = new AntPathRequestMatcher(
                DEFAULT_AUTHORIZATION_ENDPOINT_URI, HttpMethod.GET.name());
        RequestMatcher clientIdMatcher = request -> {
            String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
            return StringUtils.hasText(clientId);
        };
        RequestMatcher requestObjectMatcher = request -> {
            String requestObj = request.getParameter(ApplicationConstant.OAUTH2_PARAM_REQUEST);
            return StringUtils.hasText(requestObj);
        };

        return new AndRequestMatcher(
                authorizationRequestGetMatcher, clientIdMatcher, requestObjectMatcher);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestWrapper req = new RequestWrapper(request);
        if (this.authorizationEndpointMatcher.matches(req)) {
            try {
                String requestObj = req.getParameter(ApplicationConstant.OAUTH2_PARAM_REQUEST);
                log.trace("Received RequestURI: [" + requestObj + "]");

                ConsentResponseWrapper res = new ConsentResponseWrapper(response, Map.of(ApplicationConstant.OAUTH2_PARAM_REQUEST, requestObj));

                PushedAuthorizationRequest par = PushedAuthorizationRequestService.parsePAR(request.getQueryString());
                AuthorizeParamHelper.injectRequiredAuthorizeParameters(par, req);

                // Continue with Authorize Flow
                filterChain.doFilter(req, res);

                return;

            } catch (OAuth2AuthorizationException ex) {
                throw ex;
            } catch (Exception e) {
                throw new OAuth2AuthorizationException(new OAuth2Error("INVALID_PAR_REQUEST", e.getLocalizedMessage(), ""));
            }
        }

        // Allow filter to continue
        filterChain.doFilter(req, response);

    }

}
