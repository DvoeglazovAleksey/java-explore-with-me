DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(512) NOT NULL,
    ip  VARCHAR(50)  NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE

);