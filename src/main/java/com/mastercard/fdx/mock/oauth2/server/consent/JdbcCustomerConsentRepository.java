package com.mastercard.fdx.mock.oauth2.server.consent;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcCustomerConsentRepository implements CustomerConsentRepository {

    // @formatter:off
    private static final String COLUMN_NAMES = "consent_id, auth_code, created_date, end_date, user_id, account_ids, status";
    // @formatter:on

    private static final String TABLE_NAME = "customer_consent";

    private static final String LOAD_CUSTOMER_CONSENT_SQL = "SELECT " + COLUMN_NAMES + " FROM " + TABLE_NAME + " WHERE ";
    private static final String AUTH_CODE_FILTER = LOAD_CUSTOMER_CONSENT_SQL + "auth_code = ?";
    private static final String CUSTOMER_CONSENT_ID_FILTER = LOAD_CUSTOMER_CONSENT_SQL + "consent_id = ?";

    // @formatter:off
    private static final String INSERT_CUSTOMER_CONSENT_SQL = "INSERT INTO " + TABLE_NAME
            + "(" + COLUMN_NAMES + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_CUSTOMER_CONSENT_SQL = "UPDATE " + TABLE_NAME
            + " SET auth_code = ?,end_date = ?,user_id = ?,account_ids = ? WHERE consent_id = ?";

    private static final String UPDATE_STATUS_SQL = "UPDATE " + TABLE_NAME
            + " SET status = ? WHERE consent_id = ?";
    // @formatter:on

    private final RowMapper<CustomerConsent> customerConsentRowMapper = new CustomerConsentRowMapper();

    private final JdbcOperations jdbcOperations;

    public JdbcCustomerConsentRepository(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "jdbcOperations cannot be null");
        this.jdbcOperations = jdbcOperations;

    }

    @Override
    public void save(CustomerConsent customerConsent) {
        Assert.notNull(customerConsent, "customerConsent cannot be null");
        if(findByConsentId(customerConsent.getConsentId()) != null) {
            updateCustomerConsent(customerConsent);
        } else {
            insertCustomerConsent(customerConsent);
        }
    }

    @Override
    public CustomerConsent findByConsentId(String consentId) {
        Assert.hasText(consentId, "customerConsentId cannot be empty");
        return findBy(CUSTOMER_CONSENT_ID_FILTER, consentId);
    }

    @Override
    public CustomerConsent findByAuthCode(String authCode) {
        Assert.hasText(authCode, "authCode cannot be empty");
        return findBy(AUTH_CODE_FILTER, authCode);
    }

    private void updateCustomerConsent(CustomerConsent customerConsent) {
        List<SqlParameterValue> parameters = new ArrayList<>();
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getAuthCode()));
        parameters.add(new SqlParameterValue(Types.TIMESTAMP, customerConsent.getEndDate()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getUserId()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getAccountIds()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getConsentId()));
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(UPDATE_CUSTOMER_CONSENT_SQL, pss);
    }

    private void insertCustomerConsent(CustomerConsent customerConsent) {
        List<SqlParameterValue> parameters = new ArrayList<>();
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getConsentId()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getAuthCode()));
        parameters.add(new SqlParameterValue(Types.TIMESTAMP, customerConsent.getCreatedDate()));
        parameters.add(new SqlParameterValue(Types.TIMESTAMP, customerConsent.getEndDate()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getUserId()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, customerConsent.getAccountIds()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, "ACTIVE"));
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(INSERT_CUSTOMER_CONSENT_SQL, pss);
    }

    public void updateCustomerConsent(String consentId, String status) {
        List<SqlParameterValue> parameters = new ArrayList<>();
        parameters.add(new SqlParameterValue(Types.VARCHAR, status));
        parameters.add(new SqlParameterValue(Types.VARCHAR, consentId));
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(UPDATE_STATUS_SQL, pss);
    }

    private CustomerConsent findBy(final String filter, Object... args) {
        List<CustomerConsent> result = jdbcOperations.query(
                filter, this.customerConsentRowMapper, args);
        return !result.isEmpty() ? result.get(0) : null;
    }

    public static class CustomerConsentRowMapper implements RowMapper<CustomerConsent> {

        @Override
        public CustomerConsent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CustomerConsent(
                    rs.getString("consent_id"),
                    rs.getTimestamp("created_date"),
                    rs.getTimestamp("end_date"),
                    rs.getString("auth_code"),
                    rs.getString("user_id"),
                    rs.getString("account_ids"),
                    rs.getString("status"));
        }
    }
}
