create table namespace
(
    id          varchar(255) not null,
    created_on  timestamp,
    description varchar(255),
    name        varchar(255),
    owner       varchar(255),
    version     int4,
    primary key (id)
);

create table company
(
    id                             varchar(255) not null,
    created_on                     timestamp,
    description                    varchar(255),
    name                           varchar(255),
    ruc                            varchar(255),
    sunat_password                 varchar(255),
    sunat_username                 varchar(255),
    sunat_url_factura              varchar(255),
    sunat_url_guia_remision        varchar(255),
    sunat_url_percepcion_retencion varchar(255),
    version                        int4,
    namespace_id                   varchar(255),
    primary key (id)
);

create table ubl_document
(
    id                             varchar(255) not null,
    created_on                     timestamp,
    document_id                    varchar(255),
    document_type                  varchar(255),
    error                          varchar(255),
    file_valid                     char(1),
    in_progress                    char(1),
    retries                        int4,
    ruc                            varchar(255),
    scheduled_delivery             timestamp,
    storage_cdr                    varchar(255),
    storage_file                   varchar(255),
    sunat_code                     int4,
    sunat_description              varchar(255),
    sunat_status                   varchar(255),
    sunat_ticket                   varchar(255),
    voided_line_document_type_code varchar(255),
    namespace_id                   varchar(255),
    version                        int4,
    primary key (id)
);

create table component
(
    ID            varchar(36) not null,
    entity_id     varchar(255),
    name          varchar(255),
    parent_id     varchar(255),
    provider_id   varchar(255),
    provider_type varchar(255),
    sub_type      varchar(255),
    primary key (ID)
);

create table component_config
(
    id           varchar(36) not null,
    name         varchar(255),
    value        varchar(4000),
    component_id varchar(36),
    primary key (id)
);

create table ubl_document_sunat_notes
(
    ubl_document_id varchar(255) not null,
    value           varchar(255)
);

alter table if exists namespace
drop
constraint if exists UKeq2y9mghytirkcofquanv5frf;

alter table if exists namespace
    add constraint UKeq2y9mghytirkcofquanv5frf unique (name);

alter table if exists company
drop
constraint if exists UKky32sf4btitn1rnwfyy0onr0p;

alter table if exists company
    add constraint UKky32sf4btitn1rnwfyy0onr0p unique (namespace_id, ruc);


alter table if exists company
    add constraint FKqt1bajc7vx7sx166h0i5bdory
    foreign key (namespace_id)
    references namespace
    on
delete
cascade;

alter table if exists component_config
    add constraint FK30o84r8uoxnh7wlbkw1a5mqje
    foreign key (component_id)
    references component;

alter table if exists ubl_document
    add constraint FK8lebpqiju4ech6ftq0h1ur0jq
    foreign key (namespace_id)
    references namespace
    on
delete
cascade;

alter table if exists ubl_document_sunat_notes
    add constraint FK6x9142wv16xao4un5xxgu60by
    foreign key (ubl_document_id)
    references ubl_document;


