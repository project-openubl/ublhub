CREATE TABLE APP_USER
(
    username    VARCHAR(250) NOT NULL,
    full_name   VARCHAR(250),
    password    VARCHAR(250) NOT NULL,
    permissions VARCHAR(250),
    version     int4         NOT NULL,
    PRIMARY KEY (username)
);

create table PROJECT
(
    name                           varchar(255) not null,
    description                    varchar(255),
    sunat_username                 varchar(255) not null,
    sunat_password                 varchar(255) not null,
    sunat_url_factura              varchar(255) not null,
    sunat_url_guia_remision        varchar(255) not null,
    sunat_url_percepcion_retencion varchar(255) not null,
    version                        int4         not null,
    primary key (name)
);

create table COMPANY
(
    project                        varchar(255) not null,
    ruc                            varchar(11)  not null,
    name                           varchar(255) not null,
    description                    varchar(255),
    logo_file_id                   varchar(255),
    sunat_username                 varchar(255),
    sunat_password                 varchar(255),
    sunat_url_factura              varchar(255),
    sunat_url_guia_remision        varchar(255),
    sunat_url_percepcion_retencion varchar(255),
    version                        int4         not null,
    primary key (project, ruc)
);

create table COMPONENT
(
    id            varchar(255) not null,
    name          varchar(255) not null,
    parent_id     varchar(255),
    provider_id   varchar(255),
    provider_type varchar(255),
    sub_type      varchar(255),
    project       varchar(255) not null,
    ruc           varchar(11)  null,
    primary key (id)
);

create table COMPONENT_CONFIG
(
    id           int8         not null,
    name         varchar(255),
    val          varchar(4000),
    component_id varchar(255) not null,
    primary key (id)
);

create table UBL_DOCUMENT
(
    id                             int8         not null,
    project                        varchar(255) not null,
    job_in_progress                char(1)      not null,
    xml_file_id                    varchar(255) not null,
    cdr_file_id                    varchar(255),
    created                        timestamp    not null,
    updated                        timestamp,
    version                        int4         not null,
    xml_ruc                        varchar(11),
    xml_serie_numero               varchar(50),
    xml_tipo_documento             varchar(50),
    xml_baja_codigo_tipo_documento varchar(50),
    sunat_code                     int4,
    sunat_description              varchar(255),
    sunat_status                   varchar(50),
    sunat_ticket                   varchar(50),
    error_description              varchar(255),
    error_phase                    varchar(255),
    error_recovery_action          varchar(255),
    error_count                    int4,
    primary key (id)
);

create table SUNAT_NOTE
(
    sunat_note_id int8 not null,
    val           varchar(255)
);

create table GENERATED_ID
(
    id            int8         not null,
    project       varchar(255) not null,
    ruc           varchar(11)  not null,
    document_type varchar(50)  not null,
    serie         int4         not null,
    numero        int4         not null,
    created       timestamp    not null,
    updated       timestamp,
    version       int4         not null,
    primary key (id)
);

create table QUTE_TEMPLATE
(
    id            int8          not null,
    content       varchar(4000) not null,
    template_type varchar(50)   not null,
    document_type varchar(50)   not null,
    project       varchar(255)  not null,
    ruc           varchar(11),
    primary key (id)
);

alter table if exists COMPONENT
    add constraint fk_component_project
        foreign key (project)
            references PROJECT
            on
                delete
                cascade;

alter table if exists COMPONENT_CONFIG
    add constraint fk_componentconfig_component
        foreign key (component_id)
            references COMPONENT
            on
                delete
                cascade;

alter table if exists COMPANY
    add constraint fk_company_project
        foreign key (project)
            references PROJECT
            on
                delete
                cascade;

alter table if exists UBL_DOCUMENT
    add constraint fk_ubldocument_project
        foreign key (project)
            references PROJECT
            on
                delete
                cascade;

alter table if exists SUNAT_NOTE
    add constraint fk_sunatnote_ubldocument
        foreign key (sunat_note_id)
            references UBL_DOCUMENT
            on
                delete
                cascade;


alter table if exists GENERATED_ID
    add constraint fk_generatedid_project
        foreign key (project)
            references PROJECT
            on
                delete
                cascade;

alter table if exists GENERATED_ID
    add constraint uq_generatedid_projectid_ruc_documenttype
        unique (project, ruc, document_type);
