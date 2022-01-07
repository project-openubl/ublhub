create table namespace
(
    id                             varchar(255) not null,
    name                           varchar(255) not null,
    description                    varchar(255),
    sunat_username                 varchar(255) not null,
    sunat_password                 varchar(255) not null,
    sunat_url_factura              varchar(255) not null,
    sunat_url_guia_remision        varchar(255) not null,
    sunat_url_percepcion_retencion varchar(255) not null,
    created                        timestamp    not null,
    updated                        timestamp,
    version                        int4         not null,
    primary key (id)
);

create table company
(
    id                             varchar(255) not null,
    ruc                            varchar(11)  not null,
    name                           varchar(255) not null,
    description                    varchar(255),
    sunat_username                 varchar(255) not null,
    sunat_password                 varchar(255) not null,
    sunat_url_factura              varchar(255) not null,
    sunat_url_guia_remision        varchar(255) not null,
    sunat_url_percepcion_retencion varchar(255) not null,
    namespace_id                   varchar(255) not null,
    created                        timestamp    not null,
    updated                        timestamp,
    version                        int4         not null,
    primary key (id)
);

create table component
(
    id            varchar(36)  not null,
    name          varchar(255) not null,
    parent_id     varchar(255),
    provider_id   varchar(255),
    provider_type varchar(255),
    sub_type      varchar(255),
    namespace_id  varchar(255) not null,
    primary key (id)
);

create table component_config
(
    id           varchar(36) not null,
    name         varchar(255),
    value        varchar(4000),
    component_id varchar(36) not null,
    primary key (id)
);

create table ubl_document
(
    id                             varchar(255) not null,

    in_progress                    char(1)      not null,
    scheduled_delivery             timestamp,
    file_valid                     char(1),
    error                          varchar(255),
    retries                        int4,

    ruc                            varchar(11),
    document_id                    varchar(50),
    document_type                  varchar(50),
    voided_line_document_type_code varchar(50),

    storage_file                   varchar(255),
    storage_cdr                    varchar(255),

    sunat_ticket                   varchar(50),
    sunat_status                   varchar(50),
    sunat_code                     int4,
    sunat_description              varchar(255),

    namespace_id                   varchar(255) not null,
    created                        timestamp    not null,
    updated                        timestamp,
    version                        int4         not null,
    primary key (id)
);

create table ubl_document_sunat_notes
(
    ubl_document_id varchar(255) not null,
    value           varchar(255)
);

create table generated_id
(
    id            varchar(255) not null,
    ruc           varchar(11)  not null,
    document_type varchar(50)  not null,
    serie         int4         not null,
    numero        int4         not null,
    namespace_id  varchar(255) not null,
    created       timestamp    not null,
    updated       timestamp,
    version       int4         not null,
    primary key (id)
);

alter table if exists namespace
drop
constraint if exists UKeq2y9mghytirkcofquanv5frf;

alter table if exists namespace
    add constraint UKeq2y9mghytirkcofquanv5frf unique (name);

alter table if exists component
    add constraint FKegknd206umu3ad5to4ekp3aja
    foreign key (namespace_id)
    references namespace;

alter table if exists component_config
    add constraint FK30o84r8uoxnh7wlbkw1a5mqje
    foreign key (component_id)
    references component;

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


alter table if exists generated_id
    add constraint FKuln89tn2t5teiufir1dsq1ka
    foreign key (namespace_id)
    references namespace;

alter table if exists generated_id
    add constraint UK4hs3cb8j320vu5apl2fb06dde
    unique (namespace_id, ruc, document_type);
