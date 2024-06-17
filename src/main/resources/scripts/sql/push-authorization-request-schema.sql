CREATE TABLE push_authorization_request (
    uri varchar(100) NOT NULL,
    duration NUMERIC(10), /* Seconds */
    end_date TIMESTAMP NOT NULL,
    request_params varchar(10000) NOT NULL,
    authorization_details varchar(10000) NOT NULL,
    PRIMARY KEY (uri)
);
