![CI](https://github.com/project-openubl/xsender-server/workflows/CI/badge.svg)
[![Docker Repository on Quay](https://quay.io/repository/projectopenubl/xsender-server/status "Docker Repository on Quay")](https://quay.io/repository/projectopenubl/xsender-server)

# xsender-server

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/

## Running the application in dev mode

### Init Keycloak

Keycloak is the Authentication system:

```shell script
docker run -p 8180:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin quay.io/projectopenubl/openubl-keycloak-theme
```

### Init PostgreSQL

PostgreSQL is used for the application:

```shell script
docker run -p 5432:5432 -e POSTGRES_USER=xsender_username -e POSTGRES_PASSWORD=xsender_password -e POSTGRES_DB=xsender_db postgres:13.1
```

### Init server

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

## License

- [Eclipse Public License - v 2.0](./LICENSE)
