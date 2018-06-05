# --- !Ups
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name varchar(255) NOT NULL
);

# --- !Downs
DROP TABLE employees;
