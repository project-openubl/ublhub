alter table if exists PROJECT
    add column if not exists sunat_client_id varchar(255),
    add column if not exists sunat_client_secret varchar(255);

alter table if exists COMPANY
    add column if not exists sunat_client_id varchar(255),
    add column if not exists sunat_client_secret varchar(255);
