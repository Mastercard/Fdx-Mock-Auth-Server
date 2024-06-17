package com.mastercard.fdx.mock.oauth2.server.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

public class FdxUserDetailsManager implements UserDetailsManager {

    @Autowired
    FdxUserService fdxUserService;

    @Override
    public void createUser(UserDetails user) {
        throw new UnsupportedOperationException("createUser() method currently not implemented");
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("updateUser() method currently not implemented");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("deleteUser() method currently not implemented");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("changePassword() method currently not implemented");
    }

    @Override
    public boolean userExists(String username) {
        try {
            loadUserByUsername(username);
            return true;
        } catch (UsernameNotFoundException ex) {
            return false;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = fdxUserService.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return User.withUsername(user.getUserId())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
