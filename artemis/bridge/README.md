# Artemis Edge Demo - AMQP Bridge

## Environment setup & build

Set the domain env vars

```
set HUB01_DOMAIN="<insert_domain_here>"
set HUB02_DOMAIN="<insert_domain_here>"
set SPOKE01_DOMAIN="<insert_domain_here>"
set SPOKE02_DOMAIN="<insert_domain_here>"
set SPOKE03_DOMAIN="<insert_domain_here>"
```

Build the bridge project

```
set PROJECT_ROOT="%CD%" # This should be the root of the git repo.
cd "%PROJECT_ROO%\java\amqp-bridge"
mvn clean package -Dquarkus.package.jar.type=uber-jar
```

## Spoke 01 Bridges

Copy the "%PROJECT_ROOT%\java\amqp-bridge\target\amqp-bridge-1.0.0-SNAPSHOT-runner.jar" to the spoke-01 broker VM. You can place it wherever you'd like. Just adjust the path in each of the following `java ... -jar <path>` commands.

Copy the "%PROJECT_ROOT%\artemis\tls\spoke-01-broker-truststore.jks" to the "%ARTEMIS_INSTALL%\broker\etc" directory.

Copy the "%PROJECT_ROOT%\java\artemis-extensions\target\artemis-extensions-1.0.0-SNAPSHOT.jar" to the "%ARTEMIS_INSTALL%\broker\lib" directory.

Edit the "%ARTEMIS_INSTALL%\broker\etc\broker.xml" file and add the following XML anywhere under the `<core>` tag:

```
<broker-plugins>
  <broker-plugin class-name="com.redhat.examples.artemis.core.StaticHeaderPlugin">
    <property key="HEADER_NAME" value="Pedigree"/>
    <property key="HEADER_VALUE" value="spoke-01"/>
  </broker-plugin>
</broker-plugins>
```

Run the bridges using the commands below:

```
set ARTEMIS_INSTALL_FS=<path to artmemis install with / instead of \>
java \
  '-Dsource.address=messages.ALL.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-01-all-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-01-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-01 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-01-all-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NY.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-01-ny-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-01-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-01 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-01-ny-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NJ.#' \
  -Dsource.pedigree=spoke-01 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-01-nj-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-01-nj-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-01-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=spoke-01 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-01-ct-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-01-ct-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-01-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar
```

## Spoke 02 Bridges

Copy the "%PROJECT_ROOT%/java/amqp-bridge/target/amqp-bridge-1.0.0-SNAPSHOT-runner.jar" to the spoke-02 broker VM. You can place it wherever you'd like. Just adjust the path in each of the following `java ... -jar <path>` commands.

Copy the "%PROJECT_ROOT%/artemis/tls/spoke-02-broker-truststore.jks" to the "%ARTEMIS_INSTALL%\broker\etc" directory.

Copy the "%PROJECT_ROOT%\java\artemis-extensions\target\artemis-extensions-1.0.0-SNAPSHOT.jar" to the "%ARTEMIS_INSTALL%\broker\lib" directory.

Edit the "%ARTEMIS_INSTALL%\broker\etc\broker.xml" file and add the following XML anywhere under the `<core>` tag:

```
<broker-plugins>
  <broker-plugin class-name="com.redhat.examples.artemis.core.StaticHeaderPlugin">
    <property key="HEADER_NAME" value="Pedigree"/>
    <property key="HEADER_VALUE" value="spoke-02"/>
  </broker-plugin>
</broker-plugins>
```

Run the bridges using the commands below:

```
set ARTEMIS_INSTALL_FS=<path to artmemis install with / instead of \>
java \
  '-Dsource.address=messages.ALL.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-02-all-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-02-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-02 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-02-all-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NJ.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-02-nj-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-02-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-02 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-02-nj-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NY.#' \
  -Dsource.pedigree=spoke-02 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-02-ny-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-02-ny-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-02-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=spoke-02 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-02-ct-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-02-ct-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-02-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar
```

## Spoke 03 Bridges

Copy the "%PROJECT_ROOT%/java/amqp-bridge/target/amqp-bridge-1.0.0-SNAPSHOT-runner.jar" to the spoke-03 broker VM. You can place it wherever you'd like. Just adjust the path in each of the following `java ... -jar <path>` commands.

Copy the "%PROJECT_ROOT%/artemis/tls/spoke-03-broker-truststore.jks" to the "%ARTEMIS_INSTALL%\broker\etc" directory.

Copy the "%PROJECT_ROOT%\java\artemis-extensions\target\artemis-extensions-1.0.0-SNAPSHOT.jar" to the "%ARTEMIS_INSTALL%\broker\lib" directory.

Edit the "%ARTEMIS_INSTALL%\broker\etc\broker.xml" file and add the following XML anywhere under the `<core>` tag:

```
<broker-plugins>
  <broker-plugin class-name="com.redhat.examples.artemis.core.StaticHeaderPlugin">
    <property key="HEADER_NAME" value="Pedigree"/>
    <property key="HEADER_VALUE" value="spoke-03"/>
  </broker-plugin>
</broker-plugins>
```

Run the bridges using the commands below:

```
set ARTEMIS_INSTALL_FS=<path to artmemis install with / instead of \>
java \
  '-Dsource.address=messages.ALL.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-03-all-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-03-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-03 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-03-all-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=hub-01 \
  "-Dsource.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-03-ct-receiver&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-03-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-03 \
  "-Dtarget.uri=amqp://localhost:5672?jms.clientID=spoke-03-ct-receiver&jms.username=admin&jms.password=admin" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NY.#' \
  -Dsource.pedigree=spoke-03 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-03-ny-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-03-ny-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-03-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar

java \
  '-Dsource.address=messages.NJ.#' \
  -Dsource.pedigree=spoke-03 \
  "-Dsource.uri=amqp://localhost:5672?jms.clientID=spoke-03-nj-sender&jms.username=admin&jms.password=admin" \
  -Dtarget.pedigree=hub-01 \
  "-Dtarget.uri=amqps://hub-01-broker-amqps-acceptor-0-svc-rte-artemis.%HUB01_DOMAIN%:443?jms.clientID=spoke-03-nj-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=%ARTEMIS_INSTALL_FS%/broker/etc/spoke-03-broker-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar ./amqp-bridge-1.0.0-SNAPSHOT-runner.jar
```
