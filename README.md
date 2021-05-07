![CI](https://github.com/project-openubl/xsender-server/workflows/CI/badge.svg)
[![Docker Repository on Quay](https://quay.io/repository/projectopenubl/xsender-server/status "Docker Repository on Quay")](https://quay.io/repository/projectopenubl/xsender-server)

# XSender server

Envía tus XMLs a la SUNAT de manera fácil.

## Iniciar el servidor en modo desarrollo

Clona el repositorio:

```shell
git clone https://github.com/project-openubl/xsender-server
```

### Inicia las dependencias

XSender server necesita:

- [PostgreSQL](https://www.postgresql.org/)
- [Keycloak](https://www.keycloak.org/)
- [Amazon S3](https://aws.amazon.com/s3/) o [Minio](https://min.io/)
- [Apache kafka](https://kafka.apache.org/)

Puede iniciar los servicios requeridos utilizando `docker-compose.yml`:

```shell
docker-compose up
```

### Configura Kafka-connect

Una vez que todas las dependencias fueron iniciadas usando `docker-compose.yml` debes de configurar `Kafka connect`.

Atre un terminal y ejecuta el siguiente comando:

```shell
curl 'localhost:8083/connectors/' -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
-d '{
   "name":"postgresql-connector",
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
      "transforms.outbox.route.topic.replacement": "outbox.event.${routedByValue}",
      "transforms.outbox.table.field.event.timestamp": "timestamp",
      "key.converter": "org.apache.kafka.connect.json.JsonConverter",
      "key.converter.schemas.enable": "false",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false"
   }
}'
```

### Inicia el servidor

Puedes iniciar el servidor en modo desarrollo usando el comando:

```shell script
./mvnw quarkus:dev
```

### Links

Una vez iniciado el servidor de desarrollo puedes acceder a los siguientes links:

- http://localhost:8080/
- http://localhost:8080/q/swagger-ui/

# License

- [Eclipse Public License - v 2.0](./LICENSE)
