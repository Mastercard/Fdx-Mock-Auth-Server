package com.mastercard.fdx.mock.oauth2.server.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestWrapperTest {

    @Test
    void testRequestParamUpdate() {

        String[] aData = {"a1"};
        String[] bData = {"b1", "b2"};
        String[] cData = {"c1", "c2", "c3"};
        String[] xData = {"x1", "x2", "x3", "x4"};

        Map<String, String[]> stringMap = Map.of("a", aData, "b", bData, "c", cData);

        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getParameterMap()).thenReturn(stringMap);

        // Confirm we have mock  that mimics the fact we CANNOT alter the MAP from getParameterMap()
        var map = req.getParameterMap();
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> map.put("x", xData));
        assertNotNull(ex);

        // Wrap it and confirm we have original values and can update / add params
        RequestWrapper reqWrap = new RequestWrapper(req);
        assertEquals(3, reqWrap.getParameterMap().size());
        assertArrayEquals(aData, reqWrap.getParameterMap().get("a"));
        assertArrayEquals(bData, reqWrap.getParameterMap().get("b"));
        assertArrayEquals(cData, reqWrap.getParameterMap().get("c"));

        reqWrap.getParameterMap().put("x", xData);
        assertEquals(4, reqWrap.getParameterMap().size());
        assertArrayEquals(xData, reqWrap.getParameterMap().get("x"));
    }

}
