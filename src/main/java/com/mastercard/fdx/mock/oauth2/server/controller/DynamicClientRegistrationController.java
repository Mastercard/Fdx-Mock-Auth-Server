package com.mastercard.fdx.mock.oauth2.server.controller;

import com.mastercard.fdx.mock.oauth2.server.common.ErrorResponse;
import com.mastercard.fdx.mock.oauth2.server.service.DynamicClientRegistrationService;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/fdx/v6")
public class DynamicClientRegistrationController {

    @Autowired
    DynamicClientRegistrationService dcrService;

    /**
     * Below method performs the DCR based on the json request body as per fdx.
     * @param clientRegistrationReq
     * @return
     */
    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@RequestBody String clientRegistrationReq) {
        try {
            clientRegistrationReq = Jsoup.clean(StringEscapeUtils.escapeHtml4(clientRegistrationReq), Safelist.basic());
             return dcrService.register(clientRegistrationReq);
        } catch (ErrorResponse ex) {
            return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Below method performs the modify client based on the json request body as per fdx. ClientId should be valid.
     * @param clientModificationReq
     * @param clientId
     * @param authorization
     * @return
     */
    @PutMapping(path = "/register/{clientId}")
    public ResponseEntity<String> modify(@RequestBody String clientModificationReq,
                                         @PathVariable("clientId") String clientId,
                                         @RequestHeader(AUTHORIZATION) String authorization) {
        try {
            clientModificationReq = Jsoup.clean(StringEscapeUtils.escapeHtml4(clientModificationReq), Safelist.basic());
            return dcrService.modify(clientModificationReq, authorization, clientId);
        } catch (ErrorResponse ex) {
            return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Below method retrieves the client information based on the registered clientId.
     * @param clientId
     * @param authorization
     * @return
     */
    @GetMapping(path = "/register/{clientId}", produces = "application/json")
    public ResponseEntity<String> get(@PathVariable("clientId") String clientId, @RequestHeader(AUTHORIZATION) String authorization) {
        try {
            authorization = Jsoup.clean(StringEscapeUtils.escapeHtml4(authorization), Safelist.basic());
            clientId = Jsoup.clean(StringEscapeUtils.escapeHtml4(clientId), Safelist.basic());
            String dhDcrAccessToken = authorization.replace("Bearer ", "");
            //change to RequestHeader
            return dcrService.get(clientId, dhDcrAccessToken);
        } catch (ErrorResponse ex) {
            return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Below method deletes the client by valid clientId.
     * @param clientId
     * @param authorization
     * @return
     */
    @DeleteMapping(path = "/register/{clientId}")
    public ResponseEntity<String> delete(@PathVariable("clientId") String clientId, @RequestHeader(AUTHORIZATION) String authorization) {
        try {
            return dcrService.delete(clientId, authorization);
        } catch (ErrorResponse ex) {
            return new ResponseEntity<>(ex.toString(), HttpStatus.BAD_REQUEST);
        }
    }
}
