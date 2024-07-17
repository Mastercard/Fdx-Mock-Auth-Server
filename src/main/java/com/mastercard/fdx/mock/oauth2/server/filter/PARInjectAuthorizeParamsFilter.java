package com.mastercard.fdx.mock.oauth2.server.filter;

import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.helper.AuthorizeParamHelper;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.service.PushedAuthorizationRequestService;
import com.mastercard.fdx.mock.oauth2.server.utils.ConsentResponseWrapper;
import com.mastercard.fdx.mock.oauth2.server.utils.RequestWrapper;
import com.nimbusds.oauth2.sdk.PushedAuthorizationRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
public class PARInjectAuthorizeParamsFilter extends OncePerRequestFilter {

    private final RequestMatcher authorizationEndpointMatcher = createDefaultRequestMatcher();

    @Autowired
	PushAuthorizationRequestRepository parRepo;

    private static RequestMatcher createDefaultRequestMatcher() {
        RequestMatcher authorizationRequestGetMatcher = new AntPathRequestMatcher(
                ApplicationConstant.DEFAULT_AUTHORIZATION_ENDPOINT_URI, HttpMethod.GET.name());
        RequestMatcher clientIdMatcher = request -> {
            String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
            return StringUtils.hasText(clientId);
        };
        RequestMatcher requestUriMatcher = request -> {
            String requestUri = request.getParameter(ApplicationConstant.OAUTH2_PARAM_REQUEST_URI);
            return StringUtils.hasText(requestUri);
        };

        return new AndRequestMatcher(
                authorizationRequestGetMatcher, clientIdMatcher, requestUriMatcher);
    }

    /**
     * This filter is overwritten for /oauth2/authorize authorization page initiated by client with requestUri.
     * Here, requestUri is validated for expiry and claims are injected to request object.
     * So, that it can be used at later processing.
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestWrapper req = new RequestWrapper(request);
        if (this.authorizationEndpointMatcher.matches(req)) {
            try {
                String requestUri = req.getParameter(ApplicationConstant.OAUTH2_PARAM_REQUEST_URI);
                log.trace("Received RequestURI: [" + requestUri + "]");

                ConsentResponseWrapper res = new ConsentResponseWrapper(response, Map.of(ApplicationConstant.OAUTH2_PARAM_REQUEST_URI, requestUri));
                PushAuthorizationRequestData parData = parRepo.findByRequestUri(requestUri);
                if (parData != null) {
                    if (parData.isExpired()) {
                        throw new OAuth2AuthorizationException(new OAuth2Error("EXPIRED_PAR_REQUEST", "Provided requestUri has expired.", ""));
                    }

                    PushedAuthorizationRequest par = PushedAuthorizationRequestService.parsePAR(parData.getRequestParams());
                    AuthorizeParamHelper.injectRequiredAuthorizeParameters(par, req);

                    // Continue with Authorize Flow
                    filterChain.doFilter(req, res);

                    return;
                } else {
                    log.error("RequestURI was not located.");
                    throw new OAuth2AuthorizationException(new OAuth2Error("INVALID_REQUEST_URI", "Provided RequestUri: [" + requestUri + "] was not found.", ""));
                }
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
