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

openssl req -subj '/CN=keycloak-service/C=US' -addext 'subjectAltName = DNS:keycloak-service.svc,DNS:keycloak-service.svc.cluster.local,DNS:keycloak-service.keycloak.svc,DNS:keycloak-service.keycloak.svc.cluster.local,DNS:keycloak-ingress-keycloak.apps.cluster-5wjkd.5wjkd.sandbox1749.opentlc.com' -newkey rsa:2048 -nodes -keyout ./keycloak/tls/key.pem -x509 -days 365 -out ./keycloak/tls/certificate.pem
oc -n keycloak create secret tls keycloak-tls-secret --cert ./keycloak/tls/certificate.pem --key ./keycloak/tls/key.pem

oc -n keycloak apply -f ./keycloak/keycloak.yaml

oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.username}' | base64 --decode
oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.password}' | base64 --decode

yq -p=json -oy '{"apiVersion": "k8s.keycloak.org/v2alpha1", "kind": "KeycloakRealmImport", "metadata": {"name": "artemis-keycloak"}, "spec": {"keycloakCRName": "keycloak", "realm": .}}' ./keycloak/realm-export.json | oc -n keycloak apply -f -
```

## Install/Configure AMQ Broker

```
export NAMESPACE=artemis
export DOMAIN=apps.cluster-5wjkd.5wjkd.sandbox1749.opentlc.com

ADJ_BROKER_COUNT=0
CN=hub-01-broker-*-svc-rte-${NAMESPACE}.${DOMAIN}
SAN=
SAN+=$(printf "DNS:hub-01-broker-amqps-%s-svc-rte-${NAMESPACE}.${DOMAIN}," $(seq 0 ${ADJ_BROKER_COUNT}))
SAN+=$(printf "DNS:hub-01-broker-mqtts-%s-svc-rte-${NAMESPACE}.${DOMAIN}," $(seq 0 ${ADJ_BROKER_COUNT}))
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password"

keytool -genkeypair -alias dummy -keyalg RSA -dname "CN=dummy" -keystore "./artemis/tls/hub-01-broker-truststore.jks" -storepass "password"
keytool -delete -alias dummy -keystore "./artemis/tls/hub-01-broker-truststore.jks" -storepass "password"

oc -n artemis create secret generic hub-01-broker-tls-secret --from-file=broker.ks=./artemis/tls/hub-01-broker-keystore.jks --from-file=client.ts=./artemis/tls/hub-01-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/login.config --from-file=_keycloak-js-client.json=./artemis/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/keycloak-bearer-token.json

oc -n artemis apply -f ./artemis/hub-01-broker.yaml
```

./artemis producer --url=amqp://hub-01-broker-amqp-acceptor-0-svc.artemis.svc.cluster.local:5672 --protocol=AMQP --user=alice --password=bosco --message-count=1 --message='hello world' --destination=queue://app.test --verbose

oc -n artemis run producer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.12.3 --rm=true --restart=Never -- /bin/bash
