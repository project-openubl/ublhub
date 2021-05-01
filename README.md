![CI](https://github.com/project-openubl/xsender-server/workflows/CI/badge.svg)
[![Docker Repository on Quay](https://quay.io/repository/projectopenubl/xsender-server/status "Docker Repository on Quay")](https://quay.io/repository/projectopenubl/xsender-server)

# XSender server

Send XMLs to SUNAT in a fashion way.

# Development

To start this project in development mode follow the instructions below.

## Clone repository

```shell
git clone https://github.com/project-openubl/xsender-server
```

## Start dependencies

Start the dependencies using `docker-compose.yml`:

```shell
docker-compose up
```

## Configure Kafka-connect

Open a terminal an execute 
```shell
curl 'localhost:8083/connectors/' -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
-d '{
   "name":"xsender-connector",
   "config":{
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "xsender-db",
      "database.port": "5432",
      "database.user": "xsender_username",
      "database.password": "xsender_password",
      "database.dbname": "xsender_db",
      "database.server.name": "dbserver1",
      "schema.include.list": "public",
      "table.include.list": "public.outboxevent",
      "tombstones.on.delete": "false",
      "transforms": "outbox",
      "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
      "transforms.outbox.table.fields.additional.placement": "type:header:eventType",
      "key.converter": "org.apache.kafka.connect.json.JsonConverter",
      "key.converter.schemas.enable": "false",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false"
   }
}'
```

## Init server

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

# License

- [Eclipse Public License - v 2.0](./LICENSE)
