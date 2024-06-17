package com.mastercard.fdx.mock.oauth2.server.filter;

import com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestRepository;
import com.mastercard.fdx.mock.oauth2.server.utils.PushAuthorizationRequestTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.sql.Timestamp;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PARInjectAuthorizeParamsFilterTest {

    @Mock
    PushAuthorizationRequestRepository parRepo;

    @InjectMocks
    PARInjectAuthorizeParamsFilter filter;

    @Test
    void testFilter_InvalidRequestUri() {
        HttpServletRequest request = generatePARAuthorizeRequest("CLIENT_ID", "REQUEST_URI");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        OAuth2AuthorizationException ex = assertThrows(
                OAuth2AuthorizationException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
        assertEquals("[INVALID_REQUEST_URI] Provided RequestUri: [REQUEST_URI] was not found.", ex.getMessage());
    }

    @Test
    void testFilter_ExpiredRequestUri() {
        HttpServletRequest request = generatePARAuthorizeRequest("CLIENT_ID", "REQUEST_URI");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        //Mockito.doNothing().when(filterChain).doFilter(Mockito.any(), Mockito.any());

        PushAuthorizationRequestData parData = new PushAuthorizationRequestData(
                "REQUEST_URI", 90, new Timestamp(System.currentTimeMillis() - 500), "PARAM1=VAL1", "auth");
        Mockito.when(parRepo.findByRequestUri(Mockito.anyString())).thenReturn(parData);

        OAuth2AuthorizationException ex = assertThrows(
                OAuth2AuthorizationException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
        assertEquals("[EXPIRED_PAR_REQUEST] Provided requestUri has expired.", ex.getMessage());
    }

    @Test
    void testFilter_ValidRequestUri() throws ServletException, IOException {
        HttpServletRequest request = generatePARAuthorizeRequest("CLIENT_ID", "REQUEST_URI");

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.doNothing().when(filterChain).doFilter(Mockito.any(), Mockito.any());

        PushAuthorizationRequestData parData = new PushAuthorizationRequestData(
                "REQUEST_URI",
                90,
                new Timestamp(System.currentTimeMillis() + 90000),
                PushAuthorizationRequestTestUtils.generateValidPARPostData(),
                "auth");
        Mockito.when(parRepo.findByRequestUri(Mockito.anyString())).thenReturn(parData);

        filter.doFilterInternal(request, response, filterChain);
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(Mockito.any(), Mockito.any());
    }

    private HttpServletRequest generatePARAuthorizeRequest(String clientId, String requestUri) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServletPath()).thenReturn(ApplicationConstant.DEFAULT_AUTHORIZATION_ENDPOINT_URI);

        String[] clientIds = {clientId};
        String[] requestUris = {requestUri};
        Map<String, String[]> paramMap = Map.of(
                OAuth2ParameterNames.CLIENT_ID, clientIds,
                ApplicationConstant.OAUTH2_PARAM_REQUEST_URI, requestUris);
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
