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

## Init server

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

# License

- [Eclipse Public License - v 2.0](./LICENSE)
