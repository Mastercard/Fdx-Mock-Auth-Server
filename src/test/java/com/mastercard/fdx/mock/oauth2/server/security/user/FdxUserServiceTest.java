package com.mastercard.fdx.mock.oauth2.server.security.user;

import com.mastercard.fdx.mock.oauth2.server.config.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FdxUserServiceTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    ApplicationProperties appProps;

    @InjectMocks
    FdxUserService userService;

    @Test
    void testSuccessResponse() {
        FdxUser expUser = new FdxUser("TESTUSER1", "TESTHASH1");

        String url = "http://testhost/user";
        when(appProps.getMockResServerUserUrl()).thenReturn(url);


        when(restTemplate.exchange(
                eq(url + "/" + expUser.getUserId()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FdxUser.class)
        )).thenReturn(new ResponseEntity<>(expUser, HttpStatus.OK));

        FdxUser user = userService.getUser("TESTUSER1");
        assertNotNull(user);
        assertEquals(expUser.getUserId(), user.getUserId());
        assertEquals(expUser.getPasswordHash(), user.getPasswordHash());
    }

    @Test
    void testNonFoundResponse() {

        String url = "http://testhost/user";
        when(appProps.getMockResServerUserUrl()).thenReturn(url);


        when(restTemplate.exchange(
                eq(url + "/" + "USER_NOT_EXIST"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FdxUser.class)
        )).thenReturn(new ResponseEntity<>((FdxUser) null, HttpStatus.NOT_FOUND));

        FdxUser user = userService.getUser("USER_NOT_EXIST");
        assertNull(user);
    }


}
