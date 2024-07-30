package com.mastercard.fdx.mock.oauth2.server.consent;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcCustomerConsentRepositoryTest {

    private static final String CONSENT_SCHEMA_SQL_RESOURCE = "scripts/sql/customer-consent.sql";

    private EmbeddedDatabase db;
    private JdbcOperations jdbcOperations;
    private JdbcCustomerConsentRepository jdbcCustomerConsentRepository;

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
        this.db = createDb(CONSENT_SCHEMA_SQL_RESOURCE);
        this.jdbcOperations = new JdbcTemplate(this.db);
        this.jdbcCustomerConsentRepository = new JdbcCustomerConsentRepository(this.jdbcOperations);
    }

    //----------------------------------------------------------------------------------------

    @AfterEach
    public void tearDown() {
        this.db.shutdown();
    }

    @Test
    void testFindByConsentId_NonExisting() {
        CustomerConsent customerConsent = jdbcCustomerConsentRepository.findByConsentId("Consent_ID1");
        assertNull(customerConsent);
    }

    @Test
    void testFindByConsentId_Existing() {
        Timestamp now = DateTimeHelper.now();
        CustomerConsent exp = getCustomerConsent(now);
        CustomerConsent res = jdbcCustomerConsentRepository.findByConsentId("Consent_ID1");
        assertNotNull(res);
        assertEquals(exp.getConsentId(), res.getConsentId());
        assertEquals(exp.getAuthCode(), res.getAuthCode());
        assertEquals(exp.getEndDate(), res.getEndDate());
        assertEquals(exp.getUserId(), res.getUserId());
        assertEquals(exp.getAccountIds(), res.getAccountIds());
    }

    @Test
    void testFindByAuthCode_NonExisting() {
        CustomerConsent customerConsent = jdbcCustomerConsentRepository.findByAuthCode("AUTH_CODE");
        assertNull(customerConsent);
    }

    //----------------------------------------------------------------------------------------

    @Test
    void testFindByAuthCode_Existing() {
        Timestamp now = DateTimeHelper.now();
        CustomerConsent exp = getCustomerConsent(now);
        CustomerConsent res = jdbcCustomerConsentRepository.findByAuthCode("AUTH_CODE");
        assertNotNull(res);
        assertEquals(exp.getConsentId(), res.getConsentId());
        assertEquals(exp.getAuthCode(), res.getAuthCode());
        assertEquals(exp.getEndDate(), res.getEndDate());
        assertEquals(exp.getUserId(), res.getUserId());
        assertEquals(exp.getAccountIds(), res.getAccountIds());
    }

    private CustomerConsent getCustomerConsent(Timestamp now) {
        CustomerConsent exp = new CustomerConsent(
                "Consent_ID1", now, now, "AUTH_CODE", "USER_ID", "ACC1,ACC2", "Active");
        jdbcCustomerConsentRepository.save(exp);
        return exp;
    }

    @Test
    void testSave_Existing() {
        Timestamp now = DateTimeHelper.now();
        CustomerConsent exp = getCustomerConsent(now);
        CustomerConsent exp2 = new CustomerConsent(
                "Consent_ID1", now, now, "AUTH_CODE2", "USER_ID2", "ACC1,ACC3", "Active");
        jdbcCustomerConsentRepository.save(exp2);
        CustomerConsent res = jdbcCustomerConsentRepository.findByConsentId(exp.getConsentId());
        assertNotNull(res);
        assertEquals(exp2.getConsentId(), res.getConsentId());
        assertEquals(exp2.getAuthCode(), res.getAuthCode());
        assertEquals(exp2.getEndDate(), res.getEndDate());
        assertEquals(exp2.getUserId(), res.getUserId());
        assertEquals(exp2.getAccountIds(), res.getAccountIds());
    }
}