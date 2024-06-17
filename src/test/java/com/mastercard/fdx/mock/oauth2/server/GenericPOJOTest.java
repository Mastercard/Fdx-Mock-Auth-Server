package com.mastercard.fdx.mock.oauth2.server;


import com.mastercard.fdx.mock.oauth2.server.model.request.*;
import com.mastercard.fdx.mock.oauth2.server.security.user.FdxUser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class GenericPOJOTest extends GenericTest {
    @Test
    void testPojos() throws Exception {

        List<Object> list = Arrays.asList(
                new Intermediary(),
                new RecipientApp(),
                new RegistryReference(),
				new FdxUser()

        );

        testGenericGetterSetter(list);
    }
}
