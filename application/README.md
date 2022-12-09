# Ublhub

Microservicio que administra tus XMLs emitidos a la SUNAT.

## Ejecutar en modo desarrollo

### Iniciar servidor

Puedes ejecutar la aplicación en modo desarrollo con:

```shell script
mvn compile quarkus:dev
```

### Iniciar UI

Instala las dependencias npm:

```shell
npm install --prefix src/main/webapp
```

Inicia la UI en modo desarrollo:

```shell
npm run start --prefix src/main/webapp
```

## Desplegar en Minikube

- Instala e inicia una instancia de Minikube
- Create un namespace `openubl`
- Create un PVC para la base de datos
- Despliega Ublhub

```shell
minikube start
kubectl create ns openubl
kubectl create -f src/main/kubernetes/minikube-pvc.yml -n openubl
eval $(minikube -p minikube docker-env)
mvn clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.kubernetes.namespace=openubl -DskipTests
```

Expone Ublhub usando:

```shell
minikube service ublhub -n openubl
```

## Links

- [Documentación](https://project-openubl.github.io)
- [Discusiones](https://github.com/project-openubl/ublhub/discussions)

## License

- [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
