CREATE TABLE point (
                       id SERIAL PRIMARY KEY,
                       x DOUBLE PRECISION NOT NULL,
                       y DOUBLE PRECISION NOT NULL,
                       r DOUBLE PRECISION NOT NULL,
                       got_it boolean not null
);