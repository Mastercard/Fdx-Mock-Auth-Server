package com.mastercard.fdx.mock.oauth2.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FdxMockAuthServerApplication.class)
class FdxMockAuthServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
