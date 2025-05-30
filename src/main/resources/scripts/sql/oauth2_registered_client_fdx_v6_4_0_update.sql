CREATE TABLE oauth2_registered_client_fdx_v6_4_0_update
(
    id                  VARCHAR(100) PRIMARY KEY,
    client_uri          VARCHAR(1000),
    contacts            VARCHAR(5000),
    description         VARCHAR(5000),
    duration_type       VARCHAR(1000),
    duration_period     VARCHAR(4),
    lookback_period     VARCHAR(4),
    logo_uri            VARCHAR(1000),
    registry_references JSON,
    intermediaries      JSON
);