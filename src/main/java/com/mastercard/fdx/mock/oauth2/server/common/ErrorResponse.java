package com.mastercard.fdx.mock.oauth2.server.common;

import com.github.openjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse extends Throwable {

    private final String error;
    private final String errorDescription;

    @Override
    public String toString() {
        var json = new JSONObject();
        json.put("error", error);
        json.put("error_description", errorDescription);
        return json.toString();
    }
}
