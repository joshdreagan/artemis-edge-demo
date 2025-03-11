# Artemis Edge Demo

## Setup

Create the requisite namespaces.

```
oc new-project keycloak
oc new-project artemis
```

Install the Keycloak operator into the 'keycloak' namespace.

Install the AMQ Broker operator into the 'artemis' namespace.

Set the environment variables.

```
export HUB01_DOMAIN=""
export HUB02_DOMAIN=""
export SPOKE01_DOMAIN=""
export SPOKE02_DOMAIN=""
export SPOKE03_DOMAIN=""
export KC_DOMAIN=""
```

Generate the TLS certs and stores.

```
#
# Keycloak
CN="keycloak-service"
SAN=
SAN+="DNS:keycloak-service.svc,"
SAN+="DNS:keycloak-service.svc.cluster.local,"
SAN+="DNS:keycloak-service.keycloak.svc,"
SAN+="DNS:keycloak-service.keycloak.svc.cluster.local,"
SAN+="DNS:keycloak-ingress-keycloak.${KC_DOMAIN},"
openssl req -subj "/CN=${CN}/C=US" -addext "subjectAltName = ${SAN}" -newkey rsa:2048 -nodes -keyout "./keycloak/tls/key.pem" -x509 -days 365 -out "./keycloak/tls/certificate.pem"

#
# Artemis Hub01 broker
BROKER_COUNT=0
CN=hub-01-broker-*-svc-rte-artemis.${HUB01_DOMAIN}
SAN=
SAN+=$(printf "DNS:hub-01-broker-amqps-%s-svc-rte-artemis.${HUB01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-01-broker-mqtts-%s-svc-rte-artemis.${HUB01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias "broker" -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/hub-01-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"

#
# Artemis Hub02 broker
BROKER_COUNT=0
CN=hub-02-broker-*-svc-rte-artemis.${HUB02_DOMAIN}
SAN=
SAN+=$(printf "DNS:hub-02-broker-amqps-%s-svc-rte-artemis.${HUB02_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-02-broker-mqtts-%s-svc-rte-artemis.${HUB02_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias "broker" -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-02-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/hub-02-broker-keystore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/hub-02-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"

#
# Artemis Spoke01 broker
BROKER_COUNT=0
CN=spoke-01-broker-*-svc-rte-artemis.${SPOKE01_DOMAIN}
SAN=
SAN+=$(printf "DNS:spoke-01-broker-amqps-%s-svc-rte-artemis.${SPOKE01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:spoke-01-broker-mqtts-%s-svc-rte-artemis.${SPOKE01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/spoke-01-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/spoke-01-broker-keystore.jks" -storepass "password" -file "./artemis/tls/spoke-01-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"

#
# Artemis client
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"
keytool -import -noprompt -alias "spoke-01-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/spoke-01-broker-certificate.crt"
```

## Install/Configure Keycloak

```
#
# Create PostgreSQL DB and credentials secret.
oc -n keycloak apply -f "./keycloak/postgresql.yaml"
oc -n keycloak create secret generic keycloak-db-secret --from-literal=username=admin --from-literal=password=admin

#
# Create the Keycloak TLS secret.
oc -n keycloak create secret tls keycloak-tls-secret --cert "./keycloak/tls/certificate.pem" --key "./keycloak/tls/key.pem"

#
# Create the Keycloak server.
cat "./keycloak/keycloak.yaml" | envsubst | oc -n keycloak apply -f -

#
# Create the "artemis-keycloak" realm.
yq -p=json -oy '{"apiVersion": "k8s.keycloak.org/v2alpha1", "kind": "KeycloakRealmImport", "metadata": {"name": "artemis-keycloak"}, "spec": {"keycloakCRName": "keycloak", "realm": .}}' "./keycloak/realm-export.json" | oc -n keycloak apply -f -
```

Once the server is running, you can login to the web console to grab the initial credentials from the "keycloak-initial-admin" secret, or use the commands below. __NOTE: Once you login, you'll need to create a new "admin" user that has the appropriate permissions, then delete the "temp-admin" account.__

```
oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.username}' | base64 --decode
oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.password}' | base64 --decode
```

## Install/Configure AMQ Broker

```
#
# Create the Artemis TLS secret.
oc -n artemis create secret generic broker-tls-secret --from-file=broker.ks=./artemis/tls/hub-01-broker-keystore.jks --from-file=client.ts=./artemis/tls/hub-01-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

#
# Create the OIDC JaaS configuration secret.
cat "./artemis/keycloak-bearer-token.template" | envsubst > "./artemis/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template" | envsubst > "./artemis/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template" | envsubst > "./artemis/keycloak-js-client.json"
oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/login.config --from-file=_keycloak-js-client.json=./artemis/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/keycloak-bearer-token.json


#
# Create the Artemis broker cluster.
oc -n artemis apply -f ./artemis/hub-01-broker.yaml
```


## Additional Helpful Commands

```
#
# Run an AMQP producer on OpenShift so you don't have to do TLS.
oc -n artemis run producer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.12.3 --rm=true --restart=Never -- /bin/bash

#
# In the Bash shell that comes back once the container starts, run the following command.
/opt/amq/bin/artemis producer --url=amqp://hub-01-broker-amqp-acceptor-0-svc.artemis.svc.cluster.local:5672 --protocol=AMQP --user=alice --password=bosco --message-count=1 --message='hello world' --destination=queue://app.test --verbose

#
# Run an AMQP consumer on OpenShift so you don't have to do TLS.
oc -n artemis run consumer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.12.3 --rm=true --restart=Never -- /bin/bash

#
# In the Bash shell that comes back once the container starts, run the following command.
./artemis consumer --url=amqp://hub-01-broker-amqp-acceptor-0-svc.artemis.svc.cluster.local:5672 --protocol=AMQP --user=bob --password=bosco --destination=queue://app.test --verbose
```
