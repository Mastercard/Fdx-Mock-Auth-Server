package com.mastercard.fdx.mock.oauth2.server.utils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.util.Map;

public class ConsentResponseWrapper extends HttpServletResponseWrapper {

    private final Map<String, String> additionalParams;

    public ConsentResponseWrapper(HttpServletResponse response, Map<String, String> additionalParams) {
        super(response);
        this.additionalParams = additionalParams;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        var locationBuilder = new StringBuilder(location);
        if (location.contains("consent"))
        {
            // Inject additional parameters to consent redirect
            for( Map.Entry<String,String> param : additionalParams.entrySet()) {
                locationBuilder.append("&").append(param.getKey()).append("=").append(param.getValue());
            }
        }

        super.sendRedirect(locationBuilder.toString());
    }

}
