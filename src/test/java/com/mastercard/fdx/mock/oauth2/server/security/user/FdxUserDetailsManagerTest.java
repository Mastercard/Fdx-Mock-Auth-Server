package com.mastercard.fdx.mock.oauth2.server.security.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FdxUserDetailsManagerTest {

    @Mock
    FdxUserService userService;

    @InjectMocks
    FdxUserDetailsManager userManager;

    @Test
    void testLoadUserByUsername() {
        FdxUser expUser = new FdxUser("TESTUSER1", "TESTHASH1");
        when(userService.getUser(anyString())).thenReturn(expUser);

        UserDetails user = userManager.loadUserByUsername(expUser.getUserId());
        assertNotNull(user);
        assertEquals(expUser.getUserId(), user.getUsername());
        assertEquals(expUser.getPasswordHash(), user.getPassword());
    }

    @Test
    void testLoadUserByUsernameNotFoundException() {
        when(userService.getUser(anyString())).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () -> userManager.loadUserByUsername("TEST_NON_EXISTING_USER"));
    }

    @Test
    void testUserExists() {
        FdxUser expUser = new FdxUser("TESTUSER1", "TESTHASH1");
        when(userService.getUser(anyString())).thenReturn(expUser);

        boolean user = userManager.userExists(expUser.getUserId());
        assertTrue(user);
    }

    @Test
    void testUserExistsNotFoundException() {
        when(userService.getUser(anyString())).thenReturn(null);
        boolean user = userManager.userExists("TEST_NON_EXISTING_USER");
        assertFalse(user);
    }


    @Test
    void testCreateUser() {
        assertThrows(UnsupportedOperationException.class, () -> userManager.createUser(null));
    }

    @Test
    void testUpdateUser() {
        assertThrows(UnsupportedOperationException.class, () -> userManager.updateUser(null));
    }

    @Test
    void testDeleteUser() {
        assertThrows(UnsupportedOperationException.class, () -> userManager.deleteUser(null));
    }

    @Test
    void testChangePassword() {
        assertThrows(UnsupportedOperationException.class, () -> userManager.changePassword(null, null));
    }
}