package com.mastercard.fdx.mock.oauth2.server.utils;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeHelperTest {

    @Test
    void testNow() {

        Timestamp before = new Timestamp(new Date().getTime());
        Timestamp now = DateTimeHelper.now();
        Timestamp after = new Timestamp(new Date().getTime());

        assertTrue(before.getTime() <= now.getTime());
        assertTrue(now.getTime() <= after.getTime());
    }

    @Test
    void testFromNow() {

        Timestamp before = new Timestamp(new Date().getTime());
        Timestamp now = DateTimeHelper.fromNow(500);

        assertTrue(before.getTime() < now.getTime());
        assertTrue((now.getTime() - before.getTime()) >  450);
    }
}
