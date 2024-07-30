package com.mastercard.fdx.mock.oauth2.server;


import com.mastercard.fdx.mock.oauth2.server.consent.AccountConsentResponse;
import com.mastercard.fdx.mock.oauth2.server.consent.ConsentGrant;
import com.mastercard.fdx.mock.oauth2.server.consent.CustomerConsent;
import com.mastercard.fdx.mock.oauth2.server.consent.Party;
import com.mastercard.fdx.mock.oauth2.server.model.request.*;
import com.mastercard.fdx.mock.oauth2.server.par.PushAuthorizationRequestData;
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
				new FdxUser(),
                new com.mastercard.fdx.mock.oauth2.server.common.Error("123", "123"),
                new AccountConsentResponse(),
                new ConsentGrant(),
                new CustomerConsent(),
                new Party(),
                new PushAuthorizationRequestData(),
                new PushAuthorizationRequestData(3500, "")

        );

        testGenericGetterSetter(list);
    }
}
