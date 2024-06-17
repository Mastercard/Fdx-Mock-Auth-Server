package com.mastercard.fdx.mock.oauth2.server.security.user;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class FdxUserService {

    @Autowired
    RestTemplate api;

    @Autowired
    ApplicationProperties props;

    public FdxUser getUser(String userId) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.AUTHORIZATION, props.getResourceServerAuthCode());

        var entity = new HttpEntity<>(headers);

        try {
            return api.exchange(
                    props.getMockResServerUserUrl() + "/" + userId,
                    HttpMethod.GET,
                    entity,
                    FdxUser.class).getBody();

        } catch (HttpClientErrorException ex) {
            log.error("Failed to retrieve user: [" + userId + "]", ex);
            return null;
        }
    }

}
