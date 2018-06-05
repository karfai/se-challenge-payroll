# --- !Ups
CREATE TABLE groups (
    name varchar(255) NOT NULL PRIMARY KEY,
    rate decimal NOT NULL
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name varchar(255) NOT NULL,
    group_id varchar(255) REFERENCES groups(name)
);

CREATE TABLE hours (
    date date NOT NULL,
    hours decimal NOT NULL,
    employee_id int REFERENCES employees(id)
)
# --- !Downs
DROP TABLE employees;
DROP TABLE groups;
DROP TABLE hours;
