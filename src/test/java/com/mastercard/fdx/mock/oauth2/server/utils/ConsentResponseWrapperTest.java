package com.mastercard.fdx.mock.oauth2.server.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsentResponseWrapperTest {

    public static final String CONSENT_URL = "http://test.com/consent?param1=val1";
    public static final String NON_CONSENT_URL = "http://test.com/other?param1=val1";

    @Test
    void testRequestUriAppend_noAdditionalParam_nonConsent() throws IOException {

        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        Map<String, String> additionalParams = Map.of();
        ConsentResponseWrapper crw = new ConsentResponseWrapper(res, additionalParams);
        crw.sendRedirect(NON_CONSENT_URL);

        Mockito.doNothing().when(res).sendRedirect(Mockito.anyString());
        Mockito.verify(res).sendRedirect(ArgumentMatchers.argThat(x -> {
            Assertions.assertThat(x).isNotNull();
            assertEquals(NON_CONSENT_URL, x);
            return true;
        }));
    }

    @Test
    void testRequestUriAppend_additionalParam_nonConsent() throws IOException {

        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        Map<String, String> additionalParams = Map.of("request_uri", "TEST_REQUEST_URI");
        ConsentResponseWrapper crw = new ConsentResponseWrapper(res, additionalParams);
        crw.sendRedirect(NON_CONSENT_URL);

        Mockito.doNothing().when(res).sendRedirect(Mockito.anyString());
        Mockito.verify(res).sendRedirect(ArgumentMatchers.argThat(x -> {
            Assertions.assertThat(x).isNotNull();
            assertEquals(NON_CONSENT_URL, x);
            return true;
        }));
    }

    @Test
    void testRequestUriAppend_noAdditionalParam_consent() throws IOException {

        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        Map<String, String> additionalParams = Map.of();
        ConsentResponseWrapper crw = new ConsentResponseWrapper(res, additionalParams);
        crw.sendRedirect(CONSENT_URL);

        Mockito.doNothing().when(res).sendRedirect(Mockito.anyString());
        Mockito.verify(res).sendRedirect(ArgumentMatchers.argThat(x -> {
            Assertions.assertThat(x).isNotNull();
            assertEquals(CONSENT_URL, x);
            return true;
        }));
    }

    @Test
    void testRequestUriAppend_singleAdditionalParam_consent() throws IOException {

        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        Map<String, String> additionalParams = Map.of("request_uri", "TEST_REQUEST_URI");
        ConsentResponseWrapper crw = new ConsentResponseWrapper(res, additionalParams);
        crw.sendRedirect(CONSENT_URL);

        Mockito.doNothing().when(res).sendRedirect(Mockito.anyString());
        Mockito.verify(res).sendRedirect(ArgumentMatchers.argThat(x -> {
            Assertions.assertThat(x).isNotNull();
            assertTrue(x.startsWith(CONSENT_URL));
            assertTrue(x.contains("&request_uri=TEST_REQUEST_URI"));
            return true;
        }));
    }

    @Test
    void testRequestUriAppend_multipleAdditionalParam_consent() throws IOException {

        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        Map<String, String> additionalParams =
                Map.of("request_uri", "TEST_REQUEST_URI",
                        "request", "TEST_REQUEST");
        ConsentResponseWrapper crw = new ConsentResponseWrapper(res, additionalParams);
        crw.sendRedirect(CONSENT_URL);

        Mockito.doNothing().when(res).sendRedirect(Mockito.anyString());
        Mockito.verify(res).sendRedirect(ArgumentMatchers.argThat(x -> {
            Assertions.assertThat(x).isNotNull();
            assertTrue(x.startsWith(CONSENT_URL));
            assertTrue(x.contains("&request_uri=TEST_REQUEST_URI"));
            assertTrue(x.contains("&request=TEST_REQUEST"));
            return true;
        }));
    }

}
