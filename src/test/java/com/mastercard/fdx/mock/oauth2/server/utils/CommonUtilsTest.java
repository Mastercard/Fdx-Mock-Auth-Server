package com.mastercard.fdx.mock.oauth2.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastercard.fdx.mock.oauth2.server.security.user.FdxUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CommonUtilsTest {
	@Test
	void test() {
		SecurityException ex = new SecurityException("Unauthorized");
		assertNotNull(CommonUtils.getStringOfStackTrace(ex.getStackTrace()));
	}

	@Test
	void testMapper() throws JsonProcessingException {
		FdxUser expUser = new FdxUser("USER_ID", "PASS_HASH");
		String res = CommonUtils.toString(expUser);
		FdxUser resUser = CommonUtils.convertObject(res, FdxUser.class);
		assertNotNull(resUser);
		assertEquals(expUser.getUserId(), resUser.getUserId());
		assertEquals(expUser.getPasswordHash(), resUser.getPasswordHash());
	}

	@Test
	void testMapper_WithErrorHandling() throws JsonProcessingException {

		String res = CommonUtils.toStringWithErrHandling(new BadClassTest());
		assertNull(res);

		FdxUser resUser = CommonUtils.convertObjectWithErrHandling("BAD DATA", FdxUser.class);
		assertNull(resUser);
	}

	private class BadClassTest {

		public String getData() throws JsonProcessingException {
			throw Mockito.mock(JsonProcessingException.class);
		}

	}

}
