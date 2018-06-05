# --- !Ups
INSERT INTO employees(name)
  VALUES
    ('Elvis Presley'),
    ('Buddy Holly'),
    ('Ringo Stark');

INSERT INTO groups (name, rate)
  VALUES
    ('A', 10.45),
    ('B', 25.55),
    ('C', 60.0);

INSERT INTO hours (date, hours, employee_id)
  VALUES
    ('2018-06-01', 2.5, 1),
    ('2018-06-01', 3.0, 1),
    ('2018-06-01', 8.5, 2),
    ('2018-06-01', 7.5, 3);

# --- !Downs
DELETE FROM employees;
DELETE FROM groups;
DELETE FROM hours;
