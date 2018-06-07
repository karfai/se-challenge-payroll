# --- !Ups
CREATE TABLE groups (
    name varchar(50) NOT NULL PRIMARY KEY,
    rate decimal NOT NULL
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name varchar(255) NOT NULL
);

CREATE TABLE hours (
    time_sheet_id varchar(50),
    date date NOT NULL,
    hours decimal NOT NULL,
    employee_id int REFERENCES employees(id) NOT NULL,
    group_id varchar(50) REFERENCES groups(name) NOT NULL
);

# --- !Downs
DROP TABLE hours;
DROP TABLE employees;
DROP TABLE groups;
