package com.mastercard.fdx.mock.oauth2.server.filter;

import com.github.openjson.JSONObject;
import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.utils.Jwks;
import com.mastercard.fdx.mock.oauth2.server.utils.PushAuthorizationRequestTestUtils;
import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InjectAuthorizeParamsFilterTest {

    @InjectMocks
    InjectAuthorizeParamsFilter filter;

    @Test
    void testFilter_InvalidRequest() {
        HttpServletRequest request = generateAuthorizeRequest("CLIENT_ID", "INVALID_REQUEST_JWT");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        OAuth2AuthorizationException ex = assertThrows(
                OAuth2AuthorizationException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
        assertEquals("[INVALID_PAR_REQUEST] Invalid request parameter: Invalid JWT serialization: Missing dot delimiter(s)", ex.getMessage());
    }

    @Test
    void testFilter_ValidRequest() throws ServletException, IOException {
        JWK jwk = Jwks.generateRSAJwk();
        JSONObject par = PushAuthorizationRequestTestUtils.generateValidPAR();
        String parJwt = Jwks.sign(jwk, par.toString());

        HttpServletRequest request = generateAuthorizeRequest("CLIENT_ID", parJwt);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.doNothing().when(filterChain).doFilter(Mockito.any(), Mockito.any());

        filter.doFilterInternal(request, response, filterChain);
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(Mockito.any(), Mockito.any());
    }

    private HttpServletRequest generateAuthorizeRequest(String clientId, String requestObj) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServletPath()).thenReturn(ApplicationConstant.DEFAULT_AUTHORIZATION_ENDPOINT_URI);

        Mockito.when(request.getQueryString()).thenReturn(OAuth2ParameterNames.CLIENT_ID + "=" + clientId + "&" +
                ApplicationConstant.OAUTH2_PARAM_REQUEST + "=" + requestObj);


        String[] clientIds = {clientId};
        String[] requestObjs = {requestObj};
        Map<String, String[]> paramMap = Map.of(
                OAuth2ParameterNames.CLIENT_ID, clientIds,
                ApplicationConstant.OAUTH2_PARAM_REQUEST, requestObjs);
        Mockito.when(request.getParameterMap()).thenReturn(paramMap);
        Mockito.when(request.getParameter(Mockito.anyString())).then((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            String key = (String) args[0];
            if (!paramMap.containsKey(key))
                return null;

            String[] val = paramMap.get(key);
            if (val.length > 0)
                return val[0];
            else
                return null;
        });

        return request;
    }
}
