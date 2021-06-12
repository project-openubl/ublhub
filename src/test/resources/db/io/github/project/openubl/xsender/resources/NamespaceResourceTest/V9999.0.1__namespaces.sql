insert into namespace(id, name, description, owner, created_on, version)
values ('1', 'my-namespace1', 'description1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 1),
       ('2', 'my-namespace2', 'description2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 1),
       ('3', 'my-namespace3', 'description3', 'admin', CURRENT_TIMESTAMP + INTERVAL '3 day', 1);

insert into company(id, ruc, name, created_on, namespace_id, version, sunat_username, sunat_password,
                    sunat_url_factura, sunat_url_guia_remision, sunat_url_percepcion_retencion)
values ('11', '11111111111', 'company1', CURRENT_TIMESTAMP + INTERVAL '1 day', '1', 1, 'username1', 'password1',
        'http://urlFactura1', 'http://urlGuia1', 'http://urlPercepcionRetencion1'),
       ('22', '22222222222', 'company2', CURRENT_TIMESTAMP + INTERVAL '2 day', '1', 1, 'username2', 'password2',
        'http://urlFactura2', 'http://urlGuia2', 'http://urlPercepcionRetencion2');
