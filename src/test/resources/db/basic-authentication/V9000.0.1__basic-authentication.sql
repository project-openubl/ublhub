insert into BASIC_USER(id, full_name, username, password, permissions, version)
values (nextval('hibernate_sequence'), 'Carlos Feria', 'admin',
        '$2a$10$Vu8TWiCj.qO7l8XTM8JYqOPjfF5Y4f/HbWvQwkIMA.EOtYS3ziddC', 'admin:app', 1),
       (nextval('hibernate_sequence'), 'Alice Smith', 'alice',
        '$2a$10$kycqeNLdoIhDgn5oegz.ie1QsUWD5l0cCb.4.0iDKp9x1NUjrwyAG', 'search,version:write', 1);
