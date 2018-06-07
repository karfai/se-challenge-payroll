# --- !Ups
INSERT INTO employees (name)
  VALUES
    ('Elvis Presley'),
    ('Buddy Holly'),
    ('Jimi Hendrix'),
    ('Ringo Starr'),
    ('Bob Dylan');

INSERT INTO groups (name, rate)
  VALUES
    ('A', 20.0),
    ('B', 30.0);

# --- !Downs
DELETE FROM employees;
DELETE FROM groups;
