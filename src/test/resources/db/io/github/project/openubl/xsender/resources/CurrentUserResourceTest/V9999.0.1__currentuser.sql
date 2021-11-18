insert into namespace(id, name, owner, created_on, sunat_username, sunat_password, sunat_url_factura,
                      sunat_url_guia_remision,
                      sunat_url_percepcion_retencion, version)
values ('1', 'my-namespace1', 'alice', CURRENT_TIMESTAMP + INTERVAL '1 day', 'username1', 'password1', 'http://url1',
        'http://url11', 'http://url111', 1);

insert into namespace(id, name, owner, created_on, sunat_username, sunat_password, sunat_url_factura,
                      sunat_url_guia_remision,
                      sunat_url_percepcion_retencion, version)
values ('2', 'my-namespace2', 'alice', CURRENT_TIMESTAMP + INTERVAL '2 day', 'username2', 'password2',
        'http://url2', 'http://url22', 'http://url222', 1);

insert into namespace(id, name, owner, created_on, sunat_username, sunat_password, sunat_url_factura,
                      sunat_url_guia_remision,
                      sunat_url_percepcion_retencion, version)
values ('3', 'my-namespace3', 'anotherUser', CURRENT_TIMESTAMP + INTERVAL '3 day', 'username3', 'password3',
        'http://url3', 'http://url33', 'http://url333', 1);
