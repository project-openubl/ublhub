insert into namespace(id, name, description, created_on, sunat_username, sunat_password, sunat_url_factura,
                      sunat_url_guia_remision,
                      sunat_url_percepcion_retencion, version)
values ('1', 'my-namespace1', 'description1', CURRENT_TIMESTAMP + INTERVAL '1 day', 'username1', 'password1',
        'http://url1', 'http://url11', 'http://url111', 1),
       ('2', 'my-namespace2', 'description2', CURRENT_TIMESTAMP + INTERVAL '2 day', 'username2', 'password2',
        'http://url2', 'http://url22', 'http://url222', 1),
       ('3', 'my-namespace3', 'description3', CURRENT_TIMESTAMP + INTERVAL '3 day', 'username3', 'password3',
        'http://url3', 'http://url33', 'http://url333', 1);

insert into company(id, ruc, name, created_on, namespace_id, version, sunat_username, sunat_password,
                    sunat_url_factura, sunat_url_guia_remision, sunat_url_percepcion_retencion)
values ('11', '11111111111', 'company1', CURRENT_TIMESTAMP + INTERVAL '1 day', '1', 1, 'username_company1',
        'password_company1', 'http://url1_company1', 'http://url2_company1', 'http://url3_company1'),
       ('22', '22222222222', 'company2', CURRENT_TIMESTAMP + INTERVAL '2 day', '1', 1, 'username_company2',
        'password_company2', 'http://url1_company2', 'http://url2_company2', 'http://url3_company2');
