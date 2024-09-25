package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.service.PushedAuthorizationRequestService;
import com.nimbusds.oauth2.sdk.PushedAuthorizationResponse;
import com.nimbusds.oauth2.sdk.PushedAuthorizationSuccessResponse;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/oauth2/par")
public class PushedAuthorizationRequestController {

    @Autowired
    PushedAuthorizationRequestService parService;

    /**
     * Below API is used to register consent via POST as per RFC-9126
     * @param body
     * @return
     */
    @PostMapping(value = "", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<JSONObject> handlePushedAuthorizationRequest(@RequestBody String body) {
        PushedAuthorizationResponse res = parService.processPAR(body);

        if (res instanceof PushedAuthorizationSuccessResponse)
            return new ResponseEntity<>(res.toSuccessResponse().toJSONObject(), HttpStatus.CREATED);
        else
            return new ResponseEntity<>(res.toErrorResponse().getErrorObject().toJSONObject() , HttpStatus.BAD_REQUEST);
    }

}
