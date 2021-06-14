-- NAMESPACES
insert into namespace(id, name, description, owner, created_on, version)
values ('1', 'my-namespace1', 'description1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
       ('2', 'my-namespace2', 'description2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
       ('3', 'my-namespace3', 'description3', 'admin', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);

-- COMPANIES
insert into ubl_document(id, namespace_id, document_id, in_progress, created_on, retries)
values
-- Documents in namespace 1
('11', '1', 'F-11', 'N', CURRENT_TIMESTAMP + INTERVAL '1 day', 0),
('22', '1', 'F-22', 'N', CURRENT_TIMESTAMP + INTERVAL '2 day', 0),
-- Companies in namespace 2
('33', '2', 'F-33', 'N', CURRENT_TIMESTAMP + INTERVAL '3 day', 0),
-- Companies in namespace 3
('44', '3', 'F-44', 'N', CURRENT_TIMESTAMP + INTERVAL '4 day', 0);
