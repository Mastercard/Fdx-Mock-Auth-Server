package com.mastercard.fdx.mock.oauth2.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDeletionServiceTest {
    private static final String CLIENT_ID = "clientId";
    private static final String REGISTERED_CLIENT_ID = "registeredClientId";
    private static final RegisteredClient CLIENT = mock(RegisteredClient.class);

    @Mock
    private JdbcOperations jdbcOperations;

    @Mock
    private RegisteredClientRepository registeredClientRepository;

    @InjectMocks
    private ClientDeletionService clientDeletionService;

    @Test
    void testDeleteClient() throws NoSuchFieldException, IllegalAccessException {
        when(registeredClientRepository.findByClientId(CLIENT_ID)).thenReturn(CLIENT);
        when(CLIENT.getId()).thenReturn(REGISTERED_CLIENT_ID);
        when(CLIENT.getClientId()).thenReturn(CLIENT_ID);
        when(jdbcOperations.update(anyString(), any(PreparedStatementSetter.class))).thenReturn(2).thenReturn(0).thenReturn(1);

        String response = clientDeletionService.deleteClient(CLIENT_ID);
        assertEquals(CLIENT_ID, response);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PreparedStatementSetter> paramsCaptor = ArgumentCaptor.forClass(PreparedStatementSetter.class);
        verify(jdbcOperations, times(3)).update(sqlCaptor.capture(), paramsCaptor.capture());
        verifySql("DELETE FROM FDX_OAUTH.oauth2_authorization WHERE registered_client_id = ?", REGISTERED_CLIENT_ID, sqlCaptor, paramsCaptor);
        verifySql("DELETE FROM FDX_OAUTH.oauth2_authorization_consent WHERE registered_client_id = ?", REGISTERED_CLIENT_ID, sqlCaptor, paramsCaptor);
        verifySql("DELETE FROM FDX_OAUTH.oauth2_registered_client WHERE client_id = ?", CLIENT_ID, sqlCaptor, paramsCaptor);
    }

    private static int verifyCount = 0;
    private static void verifySql(String expectedSql, String expectedParam, ArgumentCaptor<String> sqlCaptor, ArgumentCaptor<PreparedStatementSetter> paramsCaptor) throws IllegalAccessException, NoSuchFieldException {
        assertEquals(expectedSql, sqlCaptor.getAllValues().get(verifyCount));
        ArgumentPreparedStatementSetter pss = (ArgumentPreparedStatementSetter)(paramsCaptor.getAllValues().get(verifyCount));
        Field privateArgs = ArgumentPreparedStatementSetter.class.getDeclaredField("args");
        privateArgs.setAccessible(true);
        Object[] args = (Object[]) privateArgs.get(pss);
        Object arg0 = args[0];
        assertEquals(expectedParam, ((SqlParameterValue)arg0).getValue());
        verifyCount++;
    }
}