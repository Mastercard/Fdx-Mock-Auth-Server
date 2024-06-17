package com.mastercard.fdx.mock.oauth2.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ClientDeletionService {
    //These deletions need to line up with spring-security-oauth2-authorization-server
    //These deletions assume that it is running in JDBC mode vs in memory mode (for local we run jdbc mode against an in memory database so this currently holds true)
    //These deletions are correct for spring-security-oauth2-authorization-server 0.3.0 but will need to be verified whenever the jar is updated
    private static final String DELETE_REGISTERED_CLIENT_SQL = "DELETE FROM FDX_OAUTH.oauth2_registered_client WHERE client_id = ?";
    private static final String DELETE_AUTHORIZATION_CONSENT_SQL = "DELETE FROM FDX_OAUTH.oauth2_authorization_consent WHERE registered_client_id = ?";
    private static final String DELETE_AUTHORIZATION_SQL = "DELETE FROM FDX_OAUTH.oauth2_authorization WHERE registered_client_id = ?";

    //This will cause a spring wiring failure on startup if not running in JDBC mode (i.e. In memory mode)
    @Autowired
    private JdbcOperations jdbcOperations;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    public String deleteClient(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if(client == null) {
            return null;
        }
        return delete(client);
    }

    private String delete(RegisteredClient registeredClient) {
        String clientId = registeredClient.getClientId();
        PreparedStatementSetter clientIdSetter = makeSetter(clientId);
        String registeredClientId = registeredClient.getId();
        PreparedStatementSetter registeredClientIdSetter = makeSetter(registeredClientId);
        jdbcOperations.update(DELETE_AUTHORIZATION_SQL, registeredClientIdSetter);
        jdbcOperations.update(DELETE_AUTHORIZATION_CONSENT_SQL, registeredClientIdSetter);
        int deleteCount = jdbcOperations.update(DELETE_REGISTERED_CLIENT_SQL, clientIdSetter);
        return deleteCount == 1 ? clientId : null;
    }

    private static PreparedStatementSetter makeSetter(String argument) {
        List<SqlParameterValue> params = new ArrayList<>();
        params.add(new SqlParameterValue(Types.VARCHAR, argument));
        return new ArgumentPreparedStatementSetter(params.toArray());
    }
}