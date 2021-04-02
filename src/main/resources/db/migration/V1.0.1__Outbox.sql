create table OutboxEvent
(
    id                 uuid         not null,
    aggregatetype      varchar(255) not null,
    aggregateid        varchar(255) not null,
    type               varchar(255) not null,
    timestamp          timestamp    not null,
    payload            varchar(2000),
    tracingspancontext varchar(256),
    primary key (id)
);
