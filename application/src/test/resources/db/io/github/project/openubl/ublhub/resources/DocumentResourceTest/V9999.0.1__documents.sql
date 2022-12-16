-- PROJECTS
insert into project(id, name, description, sunat_username, sunat_password, sunat_url_factura, sunat_url_guia_remision,
                    sunat_url_percepcion_retencion, created, version)
values (1, 'my-project1', 'description1', 'username1', 'password1',
        'http://url1', 'http://url11', 'http://url111', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
       (2, 'my-project2', 'description2', 'username2', 'password2',
        'http://url2', 'http://url22', 'http://url222', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
       (3, 'my-project3', 'description3', 'username3', 'password3',
        'http://url3', 'http://url33', 'http://url333', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);

-- COMPANIES
insert into company(id, ruc, name, project_id, sunat_username, sunat_password, sunat_url_factura,
                    sunat_url_guia_remision, sunat_url_percepcion_retencion, created, version)
values
-- Companies in namespace 1
(11, '11111111111', 'company1', 1, 'username1', 'password1', 'http://urlFactura1', 'http://urlGuia1',
 'http://urlPercepcionRetencion1', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
(22, '12345678912', 'company2', 1, '12345678912MODDATOS', 'MODDATOS',
 'https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService',
 'https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService',
 'https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
-- Companies in namespace 2
(33, '11111111111', 'company3', 2, 'username3', 'password3', 'http://urlFactura3', 'http://urlGuia3',
 'http://urlPercepcionRetencion3', CURRENT_TIMESTAMP + INTERVAL '3 day', 1),
-- Companies in namespace 3
(44, '44444444444', 'company4', 3, 'username4', 'password4', 'http://urlFactura4', 'http://urlGuia4',
 'http://urlPercepcionRetencion4', CURRENT_TIMESTAMP + INTERVAL '4 day', 1);

-- DOCUMENTS
insert into ubl_document(id, project_id, xml_file_id, job_in_progress, created, xml_ruc, xml_serie_numero,
                         xml_tipo_documento, version)
values
-- Documents in project 1
(11, 1, '/home/ubl.xml', 'N', CURRENT_TIMESTAMP + INTERVAL '1 day', '12345678910', 'F-11', 'Invoice', 1),
(22, 1, '/home/ubl.xml', 'N', CURRENT_TIMESTAMP + INTERVAL '2 day', '12345678910', 'F-22', 'Invoice', 1),
-- Companies in namespace 2
(33, 2, '/home/ubl.xml', 'N', CURRENT_TIMESTAMP + INTERVAL '3 day', '12345678910', 'F-33', 'Invoice', 1),
-- Companies in namespace 3
(44, 3, '/home/ubl.xml', 'N', CURRENT_TIMESTAMP + INTERVAL '4 day', '12345678910', 'F-44', 'Invoice', 1);
