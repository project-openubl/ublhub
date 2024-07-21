#!/bin/sh -l
KEYCLOAK_HOME="/opt/keycloak"

while ! $KEYCLOAK_HOME/bin/kcadm.sh config credentials config --server "$KEYCLOAK_SERVER_URL" --realm master --user "$KEYCLOAK_ADMIN" --password "$KEYCLOAK_ADMIN_PASSWORD" &> /dev/null; do
  echo "Waiting for Keycloak to start up..."
  sleep 3
done

# Create realm
$KEYCLOAK_HOME/bin/kcadm.sh create realms -s realm=openubl -s enabled=true -o

# Create clients
$KEYCLOAK_HOME/bin/kcadm.sh create clients -r openubl  -f - << EOF
  {
    "clientId": "openubl-api",
    "secret": "secret"
  }
EOF

$KEYCLOAK_HOME/bin/kcadm.sh create clients -r openubl  -f - << EOF
  {
    "clientId": "openubl-ui",
    "publicClient": true,
    "redirectUris": ["*"],
    "webOrigins": ["*"]
  }
EOF

# Create user
$KEYCLOAK_HOME/bin/kcadm.sh create users -r=openubl -s username=carlos -s enabled=true
$KEYCLOAK_HOME/bin/kcadm.sh set-password -r=openubl --username carlos --new-password carlos