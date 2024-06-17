package com.mastercard.fdx.mock.oauth2.server.security.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class FdxUser {

    private String userId;
    private String passwordHash;

}
