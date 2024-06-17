CREATE TABLE customer_consent (
    consent_id varchar(100) NOT NULL,
    auth_code varchar(1000) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    user_id varchar(100) NOT NULL,
    account_ids varchar(1000),
    status varchar(100) NOT NULL,
    PRIMARY KEY (consent_id)
);

CREATE INDEX customer_consent_auth_code ON customer_consent(auth_code);
CREATE INDEX customer_consent_user_id ON customer_consent(user_id);