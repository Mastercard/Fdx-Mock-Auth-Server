package com.mastercard.fdx.mock.oauth2.server.par;

import com.mastercard.fdx.mock.oauth2.server.utils.DateTimeHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class JdbcPushAuthorizationRequestRepositoryTest {

    private static final String PAR_SCHEMA_SQL_RESOURCE = "scripts/sql/push-authorization-request-schema.sql";

    private EmbeddedDatabase db;
    private JdbcOperations jdbcOperations;
    private JdbcPushAuthorizationRequestRepository jdbcPushAuthorizationRequestRepository;

    private static EmbeddedDatabase createDb(String schema) {
        // @formatter:off
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript(schema)
                .build();
        // @formatter:on
    }

    @BeforeEach
    public void setUp() {
        this.db = createDb(PAR_SCHEMA_SQL_RESOURCE);
        this.jdbcOperations = new JdbcTemplate(this.db);
        this.jdbcPushAuthorizationRequestRepository = new JdbcPushAuthorizationRequestRepository(this.jdbcOperations);
    }

    @AfterEach
    public void tearDown() {
        this.db.shutdown();
    }

    //----------------------------------------------------------------------------------------

    @Test
    void testFindByUri_NonExisting() {
        PushAuthorizationRequestData parData = jdbcPushAuthorizationRequestRepository.findByRequestUri("URI1");
        assertNull(parData);
    }


    @Test
    void testFindByUri_Existing() {
        Timestamp now = DateTimeHelper.now();
        PushAuthorizationRequestData exp = new PushAuthorizationRequestData(
                "URI1", 90, now, "PARAM1=VAL1&PARAM2=VAL2", "auth");
        jdbcPushAuthorizationRequestRepository.save(exp);

        PushAuthorizationRequestData res = jdbcPushAuthorizationRequestRepository.findByRequestUri("URI1");
        assertNotNull(res);
        assertEquals(exp.getRequestUri(), res.getRequestUri());
        assertEquals(exp.getDuration(), res.getDuration());
        assertEquals(exp.getEndDate(), res.getEndDate());
        assertEquals(exp.getRequestParams(), res.getRequestParams());
    }
}
