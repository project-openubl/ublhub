insert into namespace(id, name, owner, created_on, version)
values ('1', 'my-namespace1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 1);

insert into namespace(id, name, owner, created_on, version)
values ('2', 'my-namespace2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 1);

insert into namespace(id, name, owner, created_on, version)
values ('3', 'my-namespace3', 'anotherUser', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);
