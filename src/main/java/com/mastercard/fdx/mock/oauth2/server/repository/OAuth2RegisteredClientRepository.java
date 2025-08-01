package com.mastercard.fdx.mock.oauth2.server.repository;

import com.mastercard.fdx.mock.oauth2.server.entity.OAuth2RegisteredClientFDX;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2RegisteredClientRepository extends JpaRepository<OAuth2RegisteredClientFDX, String> {
}
