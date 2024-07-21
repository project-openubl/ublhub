#!/bin/bash

set -e

if [[ -z "$OPENUBL_API_URL" ]]; then
  echo "You must provide OPENUBL_API_URL environment variable" 1>&2
  exit 1
fi

if [[ $AUTH_REQUIRED != "false" ]]; then
  if [[ -z "$OIDC_CLIENT_ID" ]]; then
    echo "You must provide OIDC_CLIENT_ID environment variable" 1>&2
    exit 1
  fi
  if [[ -z "$OIDC_SERVER_URL" ]]; then
    echo "You must provide OIDC_SERVER_URL environment variable" 1>&2
    exit 1
  fi
fi

# Copy the Kube API and service CA bundle to /opt/app-root/src/ca.crt if they exist

# Add Kube API CA
if [ -f "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt" ]; then
   cp /var/run/secrets/kubernetes.io/serviceaccount/ca.crt ${NODE_EXTRA_CA_CERTS}
fi

# Add service serving CA
if [ -f "/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt" ]; then
    cat /var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt >> ${NODE_EXTRA_CA_CERTS}
fi

# Add custom ingress CA if it exists
if [ -f "/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem" ]; then
    cat /etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem >> ${NODE_EXTRA_CA_CERTS}
fi

exec node --enable-source-maps server/dist/index.js
