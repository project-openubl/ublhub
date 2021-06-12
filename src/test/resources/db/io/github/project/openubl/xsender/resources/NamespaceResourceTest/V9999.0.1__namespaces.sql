insert into namespace(id, name, description, owner, created_on, version)
values ('1', 'my-namespace1', 'description1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
       ('2', 'my-namespace2', 'description2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
       ('3', 'my-namespace3', 'description3', 'admin', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);
