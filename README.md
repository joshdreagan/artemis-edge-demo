# Artemis Edge Demo

## Setup

Create the requisite namespaces.

```
oc new-project keycloak
oc new-project artemis
oc new-project metrics
```

Install the Keycloak operator into the 'keycloak' namespace.

Install both the Prometheus and Grafana operators into the 'metrics' namespace.

Install the AMQ Broker operator into the 'artemis' namespace.

Set the environment variables.

```
export KC_DOMAIN="<insert_domain_here>"
export HUB01_DOMAIN="<insert_domain_here>"
export HUB02_DOMAIN="<insert_domain_here>"
export SPOKE01_DOMAIN="<insert_domain_here>"
export SPOKE02_DOMAIN="<insert_domain_here>"
export SPOKE03_DOMAIN="<insert_domain_here>"
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
SAN+="DNS:keycloak-ingress-keycloak.${KC_DOMAIN}"
openssl req -subj "/CN=${CN}/C=US" -addext "subjectAltName = ${SAN}" -newkey rsa:2048 -nodes -keyout "./keycloak/tls/key.pem" -x509 -days 365 -out "./keycloak/tls/certificate.pem"

#
# Artemis Hub01 broker
BROKER_COUNT=0
CN=hub-01-broker-*-svc-rte-artemis.${HUB01_DOMAIN}
SAN=
SAN+=$(printf "DNS:hub-01-broker-cores-acceptor-%s-svc-rte-artemis.${HUB01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-01-broker-amqps-acceptor-%s-svc-rte-artemis.${HUB01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-01-broker-mqtts-acceptor-%s-svc-rte-artemis.${HUB01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias "broker" -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/hub-01-broker-keystore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/hub-01-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"

#
# Artemis Hub02 broker
BROKER_COUNT=0
CN=hub-02-broker-*-svc-rte-artemis.${HUB02_DOMAIN}
SAN=
SAN+=$(printf "DNS:hub-02-broker-cores-acceptor-%s-svc-rte-artemis.${HUB02_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-02-broker-amqps-acceptor-%s-svc-rte-artemis.${HUB02_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:hub-02-broker-mqtts-acceptor-%s-svc-rte-artemis.${HUB02_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias "broker" -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/hub-02-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/hub-02-broker-keystore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/hub-02-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"

#
# Artemis Spoke01 broker
BROKER_COUNT=0
CN=spoke-01-broker-*-svc-rte-artemis.${SPOKE01_DOMAIN}
SAN=
SAN+=$(printf "DNS:spoke-01-broker-cores-acceptor-%s-svc-rte-artemis.${SPOKE01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:spoke-01-broker-amqps-acceptor-%s-svc-rte-artemis.${SPOKE01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
SAN+=$(printf "DNS:spoke-01-broker-mqtts-acceptor-%s-svc-rte-artemis.${SPOKE01_DOMAIN}," $(seq 0 ${BROKER_COUNT}))
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/spoke-01-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/spoke-01-broker-keystore.jks" -storepass "password" -file "./artemis/tls/spoke-01-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/spoke-01-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"

#
# Artemis Spoke02 broker
CN=spoke-02-broker-*.${SPOKE02_DOMAIN}
SAN=
SAN+="DNS:spoke-02-broker.${SPOKE02_DOMAIN},"
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/spoke-02-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/spoke-02-broker-keystore.jks" -storepass "password" -file "./artemis/tls/spoke-02-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/spoke-02-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/spoke-02-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/spoke-02-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"

#
# Artemis Spoke03 broker
CN=spoke-03-broker-*.${SPOKE03_DOMAIN}
SAN=
SAN+="DNS:spoke-03-broker.${SPOKE03_DOMAIN},"
keytool -genkeypair -alias broker -keyalg RSA -dname "CN=${CN}" -ext "SAN=${SAN}" -keystore "./artemis/tls/spoke-03-broker-keystore.jks" -storepass "password"
keytool -export -alias "broker" -keystore "./artemis/tls/spoke-03-broker-keystore.jks" -storepass "password" -file "./artemis/tls/spoke-03-broker-certificate.crt"
keytool -import -noprompt -alias "keycloak" -keystore "./artemis/tls/spoke-03-broker-truststore.jks" -storepass "password" -file "./keycloak/tls/certificate.pem"
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/spoke-03-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/spoke-03-broker-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"

#
# Artemis client
keytool -import -noprompt -alias "hub-01-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/hub-01-broker-certificate.crt"
keytool -import -noprompt -alias "hub-02-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/hub-02-broker-certificate.crt"
keytool -import -noprompt -alias "spoke-01-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/spoke-01-broker-certificate.crt"
keytool -import -noprompt -alias "spoke-02-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/spoke-02-broker-certificate.crt"
keytool -import -noprompt -alias "spoke-03-broker" -keystore "./artemis/tls/client-truststore.jks" -storepass "password" -file "./artemis/tls/spoke-03-broker-certificate.crt"
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

Once the server is running, you can login to the OpenShift web console to grab the initial credentials from the "keycloak-initial-admin" secret, or use the commands below. You can use those credentials to login to the Keycloak admin console. __NOTE: Once you login to the Keycloak admin console, you'll need to create a new "admin" user that has the appropriate permissions, then delete the "temp-admin" account.__

```
oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.username}' | base64 --decode
oc -n keycloak get secret keycloak-initial-admin -o jsonpath='{.data.password}' | base64 --decode
```

In the Keycloak admin console, switch to the "artemis-keycloak" realm. Under Clients, select the "artemis-broker" client. Go to the "Credentials" tab and regenerate a new client secret. Copy the newly generated value and set it in the environment variable below.

```
export KC_CLIENT_SECRET="<insert_secret_here>"
```

In the Keycloak admin console, switch to the "artemis-keycloak" realm. Under Users, create a new user named "alice" with artemis-broker/producer roles. Add a new credential password with a value of "bosco".

In the Keycloak admin console, switch to the "artemis-keycloak" realm. Under Users, create a new user named "bob" with artemis-broker/consumer roles. Add a new credential password with a value of "bosco".

## Install/Configure Prometheus & Grafana

```
#
# Create/configure the Prometheus server. These steps must be completed as cluster-admin.
oc -n metrics apply -f "./prometheus/prometheus-additional-scrape-secret.yaml"
oc -n metrics apply -f "./prometheus/prometheus.yaml"

oc -n metrics create secret generic hub-01-broker-auth --from-literal=user=admin --from-literal=password=admin
oc -n metrics apply -f "./prometheus/hub-01-broker-service-monitor.yaml"

oc -n metrics create secret generic hub-02-broker-auth --from-literal=user=admin --from-literal=password=admin
oc -n metrics apply -f "./prometheus/hub-02-broker-service-monitor.yaml"

oc -n metrics create secret generic spoke-01-broker-auth --from-literal=user=admin --from-literal=password=admin
oc -n metrics apply -f "./prometheus/spoke-01-broker-service-monitor.yaml"
# End cluster-admin steps.

#
# Create/configure the Grafana server.
oc -n metrics apply -f "./grafana/grafana.yaml"
oc -n metrics expose service grafana-service
oc -n metrics apply -f "./grafana/prometheus-datasource.yaml"
oc -n metrics apply -f './grafana/grafana-*-dashboard.yaml'
```

## Install/Configure AMQ Broker

__Hub01 Broker__

```
#
# Create the Artemis TLS secret.
oc -n artemis create secret generic broker-tls-secret --from-file=broker.ks=./artemis/tls/hub-01-broker-keystore.jks --from-file=client.ts=./artemis/tls/hub-01-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

#
# Create the OIDC JaaS configuration secret.
export DIRECT_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-direct-access.json'
export BEARER_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-bearer-token.json'
export TRUSTSTORE_PATH='/etc/broker-tls-secret-volume/client.ts'
cat "./artemis/login.config" | envsubst > "./artemis/tmp/hub-01/login.config"
cat "./artemis/keycloak-bearer-token.template.json" | envsubst > "./artemis/tmp/hub-01/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template.json" | envsubst > "./artemis/tmp/hub-01/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template.json" | envsubst > "./artemis/tmp/hub-01/keycloak-js-client.json"
oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/tmp/hub-01/login.config --from-file=_keycloak-js-client.json=./artemis/tmp/hub-01/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/tmp/hub-01/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/tmp/hub-01/keycloak-bearer-token.json

#
# Create the brokerProperties secret.
cat ./artemis/hub-01-broker.template.properties | envsubst > "./artemis/tmp/hub-01/hub-01-broker.properties"
oc -n artemis create secret generic hub-01-broker-bp --from-file=broker.properties=./artemis/tmp/hub-01/hub-01-broker.properties

#
# Create the Artemis broker cluster.
oc -n artemis apply -f "./artemis/hub-01-broker.yaml"

#
# Create the Prometheus metrics endpoint for the broker.
oc -n artemis apply -f "./artemis/hub-01-prometheus-service.yaml"
```

__Hub02 Broker__

```
#
# Create the Artemis TLS secret.
oc -n artemis create secret generic broker-tls-secret --from-file=broker.ks=./artemis/tls/hub-02-broker-keystore.jks --from-file=client.ts=./artemis/tls/hub-02-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

#
# Create the OIDC JaaS configuration secret.
export DIRECT_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-direct-access.json'
export BEARER_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-bearer-token.json'
export TRUSTSTORE_PATH='/etc/broker-tls-secret-volume/client.ts'
cat "./artemis/login.config" | envsubst > "./artemis/tmp/hub-02/login.config"
cat "./artemis/keycloak-bearer-token.template.json" | envsubst > "./artemis/tmp/hub-02/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template.json" | envsubst > "./artemis/tmp/hub-02/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template.json" | envsubst > "./artemis/tmp/hub-02/keycloak-js-client.json"
oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/tmp/hub-02/login.config --from-file=_keycloak-js-client.json=./artemis/tmp/hub-02/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/tmp/hub-02/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/tmp/hub-02/keycloak-bearer-token.json

#
# Create the brokerProperties secret.
cat ./artemis/hub-02-broker.template.properties | envsubst > "./artemis/tmp/hub-02/hub-02-broker.properties"
oc -n artemis create secret generic hub-02-broker-bp --from-file=broker.properties=./artemis/tmp/hub-02/hub-02-broker.properties

#
# Create the Artemis broker cluster.
oc -n artemis apply -f "./artemis/hub-02-broker.yaml"

#
# Create the Prometheus metrics endpoint for the broker.
oc -n artemis apply -f "./artemis/hub-02-prometheus-service.yaml"
```

__Spoke01 Broker__

```
#
# Create the Artemis TLS secret.
oc -n artemis create secret generic broker-tls-secret --from-file=broker.ks=./artemis/tls/spoke-01-broker-keystore.jks --from-file=client.ts=./artemis/tls/spoke-01-broker-truststore.jks --from-literal=keyStorePassword=password --from-literal=trustStorePassword=password

#
# Create the OIDC JaaS configuration secret.
export DIRECT_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-direct-access.json'
export BEARER_KC_CONFIG='/amq/extra/secrets/oidc-jaas-config/_keycloak-bearer-token.json'
export TRUSTSTORE_PATH='/etc/broker-tls-secret-volume/client.ts'
cat "./artemis/login.config" | envsubst > "./artemis/tmp/spoke-01/login.config"
cat "./artemis/keycloak-bearer-token.template.json" | envsubst > "./artemis/tmp/spoke-01/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template.json" | envsubst > "./artemis/tmp/spoke-01/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template.json" | envsubst > "./artemis/tmp/spoke-01/keycloak-js-client.json"
oc -n artemis create secret generic oidc-jaas-config --from-file=login.config=./artemis/tmp/spoke-01/login.config --from-file=_keycloak-js-client.json=./artemis/tmp/spoke-01/keycloak-js-client.json --from-file=_keycloak-direct-access.json=./artemis/tmp/spoke-01/keycloak-direct-access.json --from-file=_keycloak-bearker-token.json=./artemis/tmp/spoke-01/keycloak-bearer-token.json

#
# Create the brokerProperties secret.
cat ./artemis/spoke-01-broker.template.properties | envsubst > "./artemis/tmp/spoke-01/spoke-01-broker.properties"
oc -n artemis create secret generic spoke-01-broker-bp --from-file=broker.properties=./artemis/tmp/spoke-01/spoke-01-broker.properties

#
# Create the Artemis broker cluster.
oc -n artemis apply -f "./artemis/spoke-01-broker.yaml"

#
# Create the Prometheus metrics endpoint for the broker.
oc -n artemis apply -f "./artemis/spoke-01-prometheus-service.yaml"
```

__Spoke02 Broker__

Prepare the installation files. __Do this on a Linux machine so the commands work, or manually make the edits to the files on a Windows machine.__

```
export DIRECT_KC_CONFIG='${artemis.instance}/etc/keycloak-direct-access.json'
export BEARER_KC_CONFIG='${artemis.instance}/etc/keycloak-bearer-token.json'
export TRUSTSTORE_PATH='${artemis.instance}/etc/spoke-02-broker-truststore.jks'

cat "./artemis/login.config" | envsubst > "./artemis/tmp/spoke-02/login.config"
cat "./artemis/keycloak-bearer-token.template.json" | envsubst > "./artemis/tmp/spoke-02/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template.json" | envsubst > "./artemis/tmp/spoke-02/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template.json" | envsubst > "./artemis/tmp/spoke-02/keycloak-js-client.json"
cat "./artemis/spoke-02-broker.template.properties" | envsubst > "./artemis/tmp/spoke-02/spoke-02-broker.properties"
```

Copy or download the Artemis installation zip to the Windows machine.

Unzip the Artemis installation zip to the desired directory.

Create the Artemis broker instance.

```
set ADVERTISED_HOST=127.0.0.1
set ARTEMIS_INSTALL="<installation_directory>"
cd "%ARTEMIS_INSTALL%"
.\bin\artemis.cmd create --name="spoke-02-broker" --user=admin --password=admin --role=admin --require-login --host=%ADVERTISED_HOST% --message-load-balancing=OFF --http-host=0.0.0.0 --no-hornetq-acceptor --no-stomp-acceptor .\broker
```

Copy the following files to the %ARTEMIS_INSTALL%\broker\etc directory:

- artemis\tls\spoke-02-broker-keystore.jks
- artemis\tls\spoke-02-broker-truststore.jks
- artemis\tmp\spoke-02\login.config
- artemis\tmp\spoke-02\keycloak-js-client.json
- artemis\tmp\spoke-02\keycloak-direct-access.json
- artemis\tmp\spoke-02\keycloak-bearer-token.json
- artemis\tmp\spoke-02\spoke-02-broker.properties

Edit the %ARTEMIS_INSTALL%\broker\etc\broker.xml file. Set the value of the `<persistence-enabled>` tag to "false" as shown below.

```
<persistence-enabled>false</persistence-enabled>
```

In the same %ARTEMIS_INSTALL%\broker\etc\broker.xml file, add the following XML snippet anywhere under the `<acceptors>` element:

```
<acceptor name="cores">tcp://0.0.0.0:61617?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=CORE;useEpoll=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-02-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
<acceptor name="amqps">tcp://0.0.0.0:5671?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-02-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
<acceptor name="mqtts">tcp://0.0.0.0:8883?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=MQTT;useEpoll=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-02-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
```

In the same %ARTEMIS_INSTALL%\broker\etc\broker.xml file, add the following XML snippet anywhere under the `<core>` element:

```
<metrics>
  <jvm-gc>true</jvm-gc>
  <jvm-memory>true</jvm-memory>
  <jvm-threads>true</jvm-threads>
  <plugin class-name="com.redhat.amq.broker.core.server.metrics.plugins.ArtemisPrometheusMetricsPlugin"/>
</metrics>
```

__Spoke03 Broker__

Prepare the installation files. __Do this on a Linux machine so the commands work, or manually make the edits to the files on a Windows machine.__

```
export DIRECT_KC_CONFIG='${artemis.instance}/etc/keycloak-direct-access.json'
export BEARER_KC_CONFIG='${artemis.instance}/etc/keycloak-bearer-token.json'
export TRUSTSTORE_PATH='${artemis.instance}/etc/spoke-03-broker-truststore.jks'

cat "./artemis/login.config" | envsubst > "./artemis/tmp/spoke-03/login.config"
cat "./artemis/keycloak-bearer-token.template.json" | envsubst > "./artemis/tmp/spoke-03/keycloak-bearer-token.json"
cat "./artemis/keycloak-direct-access.template.json" | envsubst > "./artemis/tmp/spoke-03/keycloak-direct-access.json"
cat "./artemis/keycloak-js-client.template.json" | envsubst > "./artemis/tmp/spoke-03/keycloak-js-client.json"
cat "./artemis/spoke-03-broker.template.properties" | envsubst > "./artemis/tmp/spoke-03/spoke-03-broker.properties"
```

Copy or download the Artemis installation zip to the Windows machine.

Unzip the Artemis installation zip to the desired directory.

Create the Artemis broker instance.

```
set ADVERTISED_HOST=127.0.0.1
set ARTEMIS_INSTALL="<installation_directory>"
cd "%ARTEMIS_INSTALL%"
.\bin\artemis.cmd create --name="spoke-03-broker" --user=admin --password=admin --role=admin --require-login --host=%ADVERTISED_HOST% --message-load-balancing=OFF --http-host=0.0.0.0 --no-hornetq-acceptor --no-stomp-acceptor .\broker
```

Copy the following files to the %ARTEMIS_INSTALL%\broker\etc directory:

- artemis\tls\spoke-03-broker-keystore.jks
- artemis\tls\spoke-03-broker-truststore.jks
- artemis\tmp\spoke-03\login.config
- artemis\tmp\spoke-03\keycloak-js-client.json
- artemis\tmp\spoke-03\keycloak-direct-access.json
- artemis\tmp\spoke-03\keycloak-bearer-token.json
- artemis\tmp\spoke-03\spoke-03-broker.properties

Edit the %ARTEMIS_INSTALL%\broker\etc\broker.xml file. Set the value of the `<persistence-enabled>` tag to "false" as shown below.

```
<persistence-enabled>false</persistence-enabled>
```

In the same %ARTEMIS_INSTALL%\broker\etc\broker.xml file, add the following XML snippet anywhere under the `<acceptors>` element:

```
<acceptor name="cores">tcp://0.0.0.0:61617?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=CORE;useEpoll=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-03-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
<acceptor name="amqps">tcp://0.0.0.0:5671?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-03-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
<acceptor name="mqtts">tcp://0.0.0.0:8883?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=MQTT;useEpoll=true;sslEnabled=true;keyStorePath=${artemis.instance}/etc/spoke-03-broker-keystore.jks;keyStorePassword=password;keyStoreType=PKCS12;keyStoreProvider=SUN;wantClientAuth=false;needClientAuth=false;sslAutoReload=true</acceptor>
```

In the same %ARTEMIS_INSTALL%\broker\etc\broker.xml file, add the following XML snippet anywhere under the `<core>` element:

```
<metrics>
  <jvm-gc>true</jvm-gc>
  <jvm-memory>true</jvm-memory>
  <jvm-threads>true</jvm-threads>
  <plugin class-name="com.redhat.amq.broker.core.server.metrics.plugins.ArtemisPrometheusMetricsPlugin"/>
</metrics>
```

## Additional Helpful Commands

```
#
# Run an AMQP producer on OpenShift so you don't have to do TLS.
oc -n artemis run producer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.12.3 --rm=true --restart=Never -- /bin/bash

#
# In the Bash shell that comes back once the container starts, run the following command.
/opt/amq/bin/artemis producer --url=amqp://hub-01-broker-amqp-acceptor-0-svc.artemis.svc.cluster.local:5672 --protocol=AMQP --user=alice --password=bosco --message-count=1 --message='hello world' --destination=topic://messages.ALL.5606 --verbose

#
# Run an AMQP consumer on OpenShift so you don't have to do TLS.
oc -n artemis run consumer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.12.3 --rm=true --restart=Never -- /bin/bash

#
# In the Bash shell that comes back once the container starts, run the following command.
/opt/amq/bin/artemis consumer --url=amqp://hub-01-broker-amqp-acceptor-0-svc.artemis.svc.cluster.local:5672 --protocol=AMQP --user=bob --password=bosco --destination=topic://messages.ALL.5606 --verbose
```
