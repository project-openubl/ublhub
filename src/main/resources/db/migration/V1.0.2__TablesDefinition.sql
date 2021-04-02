create table COMPANY
(
    id                             varchar(255) not null,
    created_on                     timestamp,
    description                    varchar(255),
    name                           varchar(255),
    owner                          varchar(255),
    sunat_password                 varchar(255),
    sunat_username                 varchar(255),
    sunat_url_factura              varchar(255),
    sunat_url_guia_remision        varchar(255),
    sunat_url_percepcion_retencion varchar(255),
    version                        int4,
    primary key (id)
);

create table UBL_DOCUMENT
(
    id                             varchar(255) not null,
    created_on                     timestamp,
    delivery_status                varchar(255),
    document_id                    varchar(255),
    document_type                  varchar(255),
    retries                        int4,
    ruc                            varchar(255),
    storage_cdr                    varchar(255),
    storage_file                   varchar(255),
    sunat_code                     int4,
    sunat_description              varchar(255),
    sunat_status                   varchar(255),
    sunat_ticket                   varchar(255),
    valid                          char(1),
    validation_error               varchar(255),
    voided_line_document_type_code varchar(255),
    will_retry_on                  timestamp,
    company_id                     varchar(255),
    primary key (id)
);

create table UBL_DOCUMENT_SUNAT_NOTES
(
    ubl_document_id varchar(255) not null,
    value           varchar(255)
);

create table UBL_DOCUMENT_EVENT
(
    id          varchar(255) not null,
    created_on  timestamp,
    description varchar(255),
    status      varchar(255),
    document_id varchar(255),
    primary key (id)
);


alter table if exists COMPANY drop
    constraint if exists UKrf676d3s4bqqyh8dud0uv1gof;

alter table if exists COMPANY
    add constraint UKrf676d3s4bqqyh8dud0uv1gof unique (name);

alter table if exists UBL_DOCUMENT
    add constraint FKci8icuh34c4vjwkyj81tihv5r
    foreign key (company_id)
    references COMPANY;

alter table if exists UBL_DOCUMENT_SUNAT_NOTES
    add constraint FK6x9142wv16xao4un5xxgu60by
    foreign key (ubl_document_id)
    references UBL_DOCUMENT;

alter table if exists UBL_DOCUMENT_EVENT
    add constraint FKhkjjk98wgev9l7vlccl8kg7yq
    foreign key (document_id)
    references UBL_DOCUMENT;
