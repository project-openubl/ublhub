![CI](https://github.com/project-openubl/ublhub/workflows/CI/badge.svg)
[![Docker Repository on Quay](https://quay.io/repository/projectopenubl/ublhub/status "Docker Repository on Quay")](https://quay.io/repository/projectopenubl/ublhub)

# XSender server

Envía tus XMLs a la SUNAT de manera fácil.

## Iniciar el servidor en modo desarrollo

Clona el repositorio:

```shell
git clone https://github.com/project-openubl/ublhub
```

### Inicia las dependencias

XSender server necesita:

- [PostgreSQL](https://www.postgresql.org/)
- [Keycloak](https://www.keycloak.org/)
- [Amazon S3](https://aws.amazon.com/s3/) o [Minio](https://min.io/)
- [ActiveMQ Artemis](https://activemq.apache.org/components/artemis/)

Puedes iniciar los servicios requeridos utilizando `docker-compose.yml`:

```shell
docker-compose up
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
