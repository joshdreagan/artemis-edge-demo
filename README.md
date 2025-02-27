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

openssl req -subj '/CN=test.keycloak.org/O=Test Keycloak./C=US' -newkey rsa:2048 -nodes -keyout ./keycloak/tls/key.pem -x509 -days 365 -out ./keycloak/tls/certificate.pem
oc -n keycloak create secret tls keycloak-tls-secret --cert ./keycloak/tls/certificate.pem --key ./keycloak/tls/key.pem

oc -n keycloak apply -f ./keycloak/keycloak.yaml
```

## Install/Configure AMQ Broker

```
CN=${BROKER_NAME}-*-svc-rte-${NAMESPACE}.${DOMAIN}
SAN=
SAN+=$(printf "DNS:${BROKER_NAME}-amqps-%s-svc-rte-${NAMESPACE}.${DOMAIN}," $(seq 0 ${ADJ_BROKER_COUNT}))
SAN+=$(printf "DNS:${BROKER_NAME}-mqtts-%s-svc-rte-${NAMESPACE}.${DOMAIN}," $(seq 0 ${ADJ_BROKER_COUNT}))
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password"

keytool -genkeypair -alias dummy -dname "CN=dummy" -keystore "./artemis/tls/hub-01-broker-truststore.jks" -storepass "password"
keytool -delete -alias dummy -keystore "./artemis/tls/broker.ts" -storepass "password"

oc -n artemis create secret generic hub-01-broker-tls-secret --from-file=broker.ks=./artemis/tls/hub-01-broker-keystore.jks --from-file=client.ts=./artemis/tls/hub-01-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/login.config --from-file=_keycloak-js-client.json=./artemis/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/keycloak-bearer-token.json

oc -n artemis apply -f ./artemis/hub-01-broker.yaml
```
