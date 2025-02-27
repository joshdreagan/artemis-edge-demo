# Artemis Edge Demo

## Setup

Create the requisite namespaces.

```
oc new-project keycloak
oc new-project artemis
```

Install the Keycloak operator into the 'keycloak' namespace.

Install the AMQ Broker operator into the 'artemis' namespace.

## Install/Configure Keycloak

```
oc -n keycloak apply -f ./keycloak/postgresql.yaml
oc -n keycloak create secret generic keycloak-db-secret --from-literal=username=admin --from-literal=password=admin
openssl req -subj '/CN=test.keycloak.org/O=Test Keycloak./C=US' -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 365 -out certificate.pem
oc -n keycloak create secret tls keycloak-tls-secret --cert certificate.pem --key key.pem
oc -n keycloak apply -f ./keycloak/keycloak.yaml
```

## Install/Configure AMQ Broker

```
# TODO generate the keystore & truststore
oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/login.config --from-file=_keycloak-js-client.json=./artemis/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/keycloak-bearer-token.json
```
