# Samisoft GRE local integration

This branch keeps the existing SOAP route for invoices, credit notes, debit
notes, summaries, perceptions and retentions. Only `DespatchAdvice` documents
whose `guiaUrl` points to `/v1/contribuyente/gem/comprobantes` use SUNAT's GRE
REST/OAuth flow.

The SUNAT configuration accepts two additional optional properties:

- `clientId`: API credential generated in SOL.
- `clientSecret`: API secret generated in SOL. API responses never expose it.

For GRE REST, `guiaUrl` must be:

```text
https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes
```

Build and start locally:

```shell
docker compose build ublhub
docker compose up -d
```

Persistent volumes retain PostgreSQL data and the `workspace` containing XML
and CDR files. Do not use `docker compose down -v` unless those local data are
intentionally being removed.
