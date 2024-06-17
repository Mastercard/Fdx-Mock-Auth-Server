package com.mastercard.fdx.mock.oauth2.server.utils;

import java.sql.Timestamp;
import java.util.Date;

public class DateTimeHelper {

    private DateTimeHelper() {}

    public static Timestamp now() {
        return new Timestamp(new Date().getTime());
    }

    public static Timestamp fromNow(int millisec) {
        return new Timestamp(new Date().getTime() + millisec);
    }

}
