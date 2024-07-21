## dev-env

Starting:

```shell
docker-compose -f openubl/deploy/compose/compose.yaml up
```

```shell
RUST_LOG=info cargo watch -x 'run -p openubl-cli -- server --db-user user --db-password password --oidc-auth-server-url http://localhost:9001/realms/openubl minio --storage-minio-host http://localhost:9002 --storage-minio-access-key admin --storage-minio-secret-key password'
```
