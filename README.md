# xml-sender project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

### Init Broker using Docker

```
docker run -e ARTEMIS_USERNAME=quarkus -e ARTEMIS_PASSWORD=quarkus \
-p 8161:8161 -p 61616:61616 vromero/activemq-artemis:2.9.0-alpine
```

### Init Storage Minio
```
docker run -e MINIO_ACCESS_KEY=BQA2GEXO711FVBVXDWKM -e \
MINIO_SECRET_KEY=uvgz3LCwWM3e400cDkQIH/y1Y4xgU4iV91CwFSPC \
-p 9000:9000 minio/minio server /data
```

### Init server
You can run your application in dev mode that enables live coding using:

```shell script
./mvnw install -DskipTests
./mvnw compile quarkus:dev -f distribution/ -DnoDeps
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `xml-sender-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/xml-sender-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/xml-sender-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide.

## License

- [Eclipse Public License - v 2.0](./LICENSE)

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fproject-openubl%2Fxml-sender.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fproject-openubl%2Fxml-sender?ref=badge_large)
