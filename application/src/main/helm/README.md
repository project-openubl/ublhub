# ublhub

## TL;DR

```
helm repo add openubl https://gitlab.com/api/v4/projects/40686221/packages/helm/stable
helm install my-release openubl/ublhub
```

## Pre requisites

- Kubernetes 1.19+
- Helm 3.2.0+

## Installing the chart

To install the chart with the release name `my-release`:

```
helm repo add bitnami https://gitlab.com/api/v4/projects/40686221/packages/helm/stable
helm install my-release openubl/ublhub
```

> Tip: List all releases using `helm list`

## Uninstalling the Chart

To uninstall/delete the `my-release` deployment:

```
helm delete my-release
```

## Parameters

## Database parameters

| Key | Type | Default | Description                               |
|-----|------|-------|-------------------------------------------|
| database.database | string | `"ublhub_db"` | Database's name                           |
| database.hostname | string | `""` | If NULL we will create a database for you |
| database.password | string | `"db_password"` |                                           |
| database.port | int | `5432` |                                           |
| database.username | string | `"db_username"` |                                           |

## Openshift parameters

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| openshift.enabled | bool | `false` |  |

## Resource limits parameters

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| resources.limits.cpu | string | `"500m"` |  |
| resources.limits.memory | string | `"512Mi"` |  |
| resources.requests.cpu | string | `"250m"` |  |
| resources.requests.memory | string | `"64Mi"` |  |

## Global parameters

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` |  |
| autoscaling.enabled | bool | `false` |  |
| autoscaling.maxReplicas | int | `100` |  |
| autoscaling.minReplicas | int | `1` |  |
| autoscaling.targetCPUUtilizationPercentage | int | `80` |  |
| autoscaling.targetMemoryUtilizationPercentage | int | `80` |  |
| extraEnvFrom | string | `""` |  |
| fullnameOverride | string | `""` |  |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"quay.io/projectopenubl/ublhub"` |  |
| image.tag | string | `""` |  |
| ingress.annotations | object | `{}` |  |
| ingress.enabled | bool | `false` |  |
| ingress.hosts[0].host | string | `"chart-example.local"` |  |
| ingress.hosts[0].paths | list | `[]` |  |
| ingress.tls | list | `[]` |  |
| nameOverride | string | `""` |  |
| nodeSelector | object | `{}` |  |
| podAnnotations | object | `{}` |  |
| replicaCount | int | `1` |  |
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |
| tolerations | list | `[]` |  |

## Use an external database

Sometimes, you may want to have Ublhub connect to an external database rather than a database within your cluster -
for example, when using a managed database service, or when running a single database server for all your applications.
To do this, set the `database.hostname` parameter to your hostname e.g. `myhost.com`.

## License

Copyright © 2021 Project OpenUBL

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
