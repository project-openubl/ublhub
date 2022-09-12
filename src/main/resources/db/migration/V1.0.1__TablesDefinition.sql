CREATE TABLE APP_USER
(
    id          int8         NOT NULL,
    full_name   VARCHAR(250),
    username    VARCHAR(250) NOT NULL,
    password    VARCHAR(250) NOT NULL,
    permissions VARCHAR(250),
    version     int8         NOT NULL,
    PRIMARY KEY (id)
);

alter table APP_USER
    add constraint uq_appuser_username unique (username);


create table PROJECT
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
    project_id                     varchar(255) not null,
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
    project_id    varchar(255) null,
    company_id    varchar(255) null,
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
    id              varchar(255) not null,
    job_in_progress char(1)      not null,
    xml_file_id     varchar(255) not null,
    cdr_file_id     varchar(255),
    project_id      varchar(255) not null,
    created         timestamp    not null,
    updated         timestamp,
    version         int4         not null,
    primary key (id)
);

create table xml_file_content
(
    document_id                varchar(255) not null,
    ruc                        varchar(11)  not null,
    serie_numero               varchar(50)  not null,
    tipo_documento             varchar(50)  not null,
    baja_codigo_tipo_documento varchar(50),
    created                    timestamp    not null,
    updated                    timestamp,
    version                    int4         not null,
    primary key (document_id)
);

create table sunat_response
(
    document_id varchar(255) not null,
    code        int4,
    description varchar(255),
    status      varchar(50),
    ticket      varchar(50),
    created     timestamp    not null,
    updated     timestamp,
    version     int4         not null,
    primary key (document_id)
);

create table sunat_response_notes
(
    sunat_response_id varchar(255) not null,
    value             varchar(255)
);

create table job_error
(
    document_id           varchar(255) not null,
    description           varchar(255),
    phase                 varchar(255) not null,
    recovery_action       varchar(255),
    recovery_action_count int4         not null,
    created               timestamp    not null,
    updated               timestamp,
    version               int4         not null,
    primary key (document_id)
);

create table generated_id
(
    id            varchar(255) not null,
    ruc           varchar(11)  not null,
    document_type varchar(50)  not null,
    serie         int4         not null,
    numero        int4         not null,
    project_id    varchar(255) not null,
    created       timestamp    not null,
    updated       timestamp,
    version       int4         not null,
    primary key (id)
);

alter table if exists project
drop
constraint if exists UKeq2y9mghytirkcofquanv5frf;

alter table if exists project
    add constraint UKeq2y9mghytirkcofquanv5frf unique (name);

alter table if exists component
    add constraint FKegknd206umu3ad5to4ekp3aja
    foreign key (project_id)
    references project;

alter table if exists component
    add constraint FKegknd206umu3ad5to4ekp3ajb
    foreign key (company_id)
    references company;

alter table if exists component_config
    add constraint FK30o84r8uoxnh7wlbkw1a5mqje
    foreign key (component_id)
    references component;

alter table if exists company
drop
constraint if exists UKky32sf4btitn1rnwfyy0onr0p;

alter table if exists company
    add constraint UKky32sf4btitn1rnwfyy0onr0p unique (project_id, ruc);


alter table if exists company
    add constraint FKqt1bajc7vx7sx166h0i5bdory
    foreign key (project_id)
    references project
    on
delete
cascade;

alter table if exists ubl_document
    add constraint FK8lebpqiju4ech6ftq0h1ur0jq
    foreign key (project_id)
    references project
    on
delete
cascade;

alter table if exists ubl_document_sunat_notes
    add constraint FK6x9142wv16xao4un5xxgu60by
    foreign key (ubl_document_id)
    references ubl_document;


alter table if exists generated_id
    add constraint FKuln89tn2t5teiufir1dsq1ka
    foreign key (project_id)
    references project;

alter table if exists generated_id
    add constraint UK4hs3cb8j320vu5apl2fb06dde
    unique (project_id, ruc, document_type);