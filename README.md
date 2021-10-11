![CI](https://github.com/project-openubl/ublhub/workflows/CI/badge.svg)
[![Docker Repository on Quay](https://quay.io/repository/projectopenubl/ublhub/status "Docker Repository on Quay")](https://quay.io/repository/projectopenubl/ublhub)

# UblHub

Crea, firma, y envía XMLs a la SUNAT.

## Iniciar el servidor en modo desarrollo

Clona el repositorio:

```shell
git clone https://github.com/project-openubl/ublhub
```

### Requisitos
Necesitas tener instalado Docker.

### Inicia el servidor

Puedes iniciar el servidor en modo desarrollo usando el comando:

```shell script
./mvnw compile quarkus:dev
```

### Links

Una vez iniciado el servidor de desarrollo puedes acceder a los siguientes links:

- http://localhost:8080/
- http://localhost:8080/q/swagger-ui/

# License

- [Eclipse Public License - v 2.0](./LICENSE)
