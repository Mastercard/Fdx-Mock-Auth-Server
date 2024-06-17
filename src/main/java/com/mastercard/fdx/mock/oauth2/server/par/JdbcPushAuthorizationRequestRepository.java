package com.mastercard.fdx.mock.oauth2.server.par;

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

public class JdbcPushAuthorizationRequestRepository implements PushAuthorizationRequestRepository {

    // @formatter:off
    private static final String COLUMN_NAMES = "uri, "
            + "duration, "
            + "end_date, "
            + "request_params, "
            + "authorization_details";
    // @formatter:on

    private static final String TABLE_NAME = "push_authorization_request";

    private static final String LOAD_PAR_SQL = "SELECT " + COLUMN_NAMES + " FROM " + TABLE_NAME + " WHERE ";
    private static final String URI_FILTER = LOAD_PAR_SQL + "uri = ?";

    // @formatter:off
    private static final String INSERT_PAR_SQL = "INSERT INTO " + TABLE_NAME
            + "(" + COLUMN_NAMES + ") VALUES (?, ?, ?, ?, ?)";
    // @formatter:on

    private final RowMapper<PushAuthorizationRequestData> parDataRowMapper = new PushAuthorizationRequestDataRowMapper();

    private final JdbcOperations jdbcOperations;

    public JdbcPushAuthorizationRequestRepository(JdbcOperations jdbcOperations) {
        Assert.notNull(jdbcOperations, "jdbcOperations cannot be null");
        this.jdbcOperations = jdbcOperations;

    }

    @Override
    public void save(PushAuthorizationRequestData parData) {
        Assert.notNull(parData, "parData cannot be null");
        insertPushAuthorizationRequest(parData);
    }

    @Override
    public PushAuthorizationRequestData findByRequestUri(String uri) {
        Assert.hasText(uri, "uri cannot be empty");
        return findBy(URI_FILTER, uri);
    }

    private void insertPushAuthorizationRequest(PushAuthorizationRequestData parData) {
        List<SqlParameterValue> parameters = new ArrayList<>();
        parameters.add(new SqlParameterValue(Types.VARCHAR, parData.getRequestUri()));
        parameters.add(new SqlParameterValue(Types.NUMERIC, parData.getDuration()));
        parameters.add(new SqlParameterValue(Types.TIMESTAMP, parData.getEndDate()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, parData.getRequestParams()));
        parameters.add(new SqlParameterValue(Types.VARCHAR, parData.getAuthorizationDetails()));
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
        this.jdbcOperations.update(INSERT_PAR_SQL, pss);
    }

    private PushAuthorizationRequestData findBy(final String filter, Object... args) {
        List<PushAuthorizationRequestData> result = jdbcOperations.query(
                filter, this.parDataRowMapper, args);
        return !result.isEmpty() ? result.get(0) : null;
    }

    public static class PushAuthorizationRequestDataRowMapper implements RowMapper<PushAuthorizationRequestData> {

        @Override
        public PushAuthorizationRequestData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PushAuthorizationRequestData(
                    rs.getString("uri"),
                    rs.getInt("duration"),
                    rs.getTimestamp("end_date"),
                    rs.getString("request_params"),
                    rs.getString("authorization_details"));
        }
    }
}
