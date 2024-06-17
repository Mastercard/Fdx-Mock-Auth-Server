package com.mastercard.fdx.mock.oauth2.server.par;

import org.springframework.lang.Nullable;

public interface PushAuthorizationRequestRepository {

    void save(PushAuthorizationRequestData parData);

    @Nullable
    PushAuthorizationRequestData findByRequestUri(String uri);

}
