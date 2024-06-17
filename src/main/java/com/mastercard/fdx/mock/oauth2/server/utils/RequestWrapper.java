package com.mastercard.fdx.mock.oauth2.server.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.HashMap;
import java.util.Map;

public class RequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> requestParams = new HashMap<>();

    public RequestWrapper(HttpServletRequest request) {
        super(request);

        this.requestParams.putAll(request.getParameterMap());
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return requestParams;
    }
}
