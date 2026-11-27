CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE charger (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL
);

CREATE TABLE reservation (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charger_id BIGINT      NOT NULL REFERENCES charger (id),
    client_id  UUID        NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ NOT NULL,
    CONSTRAINT reservation_valid_period CHECK (end_time > start_time),
    CONSTRAINT reservation_no_overlap
        EXCLUDE USING gist (charger_id WITH =, tstzrange(start_time, end_time) WITH &&)
);

CREATE INDEX reservation_client_id_idx ON reservation (client_id);

INSERT INTO charger (name, location) VALUES
    ('King''s Cross Hub', 'London, Pancras Road'),
    ('Piccadilly Fast Charge', 'Manchester, Store Street'),
    ('Waverley Station', 'Edinburgh, Market Street');
