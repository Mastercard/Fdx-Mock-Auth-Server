package com.mastercard.fdx.mock.oauth2.server.par;

import com.mastercard.fdx.mock.oauth2.server.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PushAuthorizationRequestData {

    public static final String URN_PREFIX = "urn:ietf:params:oauth2:";

    private String requestUri;
    private int duration;
    private Timestamp endDate;
    private String requestParams;
    private String authorizationDetails;

    public PushAuthorizationRequestData(int duration, String requestParams) {
        this.requestUri = generateUri();
        this.duration = duration;
        this.endDate = new Timestamp(System.currentTimeMillis() + (duration * 1000L));
        this.requestParams = requestParams;
    }

    public static String generateUri() {
        return URN_PREFIX + UUID.randomUUID();
    }

    public boolean isExpired() {
        return (CommonUtils.now().compareTo(getEndDate()) > 0);
    }
}
