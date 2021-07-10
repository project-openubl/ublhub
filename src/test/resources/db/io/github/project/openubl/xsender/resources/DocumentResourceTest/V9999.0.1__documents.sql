-- NAMESPACES
insert into namespace(id, name, description, owner, created_on, version)
values ('1', 'my-namespace1', 'description1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
       ('2', 'my-namespace2', 'description2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
       ('3', 'my-namespace3', 'description3', 'admin', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);

-- COMPANIES
insert into company(id, ruc, name, created_on, namespace_id, version, sunat_username, sunat_password,
                    sunat_url_factura, sunat_url_guia_remision, sunat_url_percepcion_retencion)
values
-- Companies in namespace 1
('11', '11111111111', 'company1', CURRENT_TIMESTAMP + INTERVAL '1 day', '1', 1, 'username1', 'password1',
 'http://urlFactura1', 'http://urlGuia1', 'http://urlPercepcionRetencion1'),
('22', '12345678912', 'company2', CURRENT_TIMESTAMP + INTERVAL '2 day', '1', 1, '12345678912MODDATOS', 'MODDATOS',
 'https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService',
 'https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService',
 'https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService'),
-- Companies in namespace 2
('33', '11111111111', 'company3', CURRENT_TIMESTAMP + INTERVAL '3 day', '2', 1, 'username3', 'password3',
 'http://urlFactura3', 'http://urlGuia3', 'http://urlPercepcionRetencion3'),
-- Companies in namespace 3
('44', '44444444444', 'company4', CURRENT_TIMESTAMP + INTERVAL '4 day', '3', 1, 'username4', 'password4',
 'http://urlFactura4', 'http://urlGuia4', 'http://urlPercepcionRetencion4');

-- DOCUMENTS
insert into ubl_document(id, namespace_id, document_id, in_progress, created_on, retries, version)
values
-- Documents in namespace 1
('11', '1', 'F-11', 'N', CURRENT_TIMESTAMP + INTERVAL '1 day', 0, 1),
('22', '1', 'F-22', 'N', CURRENT_TIMESTAMP + INTERVAL '2 day', 0, 1),
-- Companies in namespace 2
('33', '2', 'F-33', 'N', CURRENT_TIMESTAMP + INTERVAL '3 day', 0, 1),
-- Companies in namespace 3
('44', '3', 'F-44', 'N', CURRENT_TIMESTAMP + INTERVAL '4 day', 0, 1);
