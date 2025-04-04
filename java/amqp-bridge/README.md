# AMQP Bridge

Creates an AMQP bridge between a source and target broker. Will add and filter on pedigree tags to prevent infinite looping when bridging in both directions.

## Running

To run in dev mode:

```aiignore
export PROJECT_ROOT="<root_of_git_repo>"
mvn quarkus:dev \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=spoke-02 \
  "-Dsource.uri=amqps://localhost:5671?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=$PROJECT_ROOT/artemis/tls/client-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-03 \
  "-Dtarget.uri=amqp://localhost:5674?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin"
```

To bundle and run as a standalone app:

```
mvn package -Dquarkus.package.jar.type=uber-jar
java \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=spoke-02 \
  "-Dsource.uri=amqps://localhost:5671?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=$PROJECT_ROOT/artemis/tls/client-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -Dtarget.pedigree=spoke-03 \
  "-Dtarget.uri=amqp://localhost:5674?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin" \
  -jar target/amqp-bridge-1.0.0-SNAPSHOT-runner.jar
```

## Application Configuration

| Property                   | Default       | Description                                                                                                          |
|:---------------------------|:--------------|:---------------------------------------------------------------------------------------------------------------------|
| `source.uri`               |               | The URI of the source broker. See URI format and options below.                                                      |
| `source.type`              | "topic"       | The type of the source address. Valid values: "queue", "topic"                                                       |
| `source.address`           |               | The source address for the AMQP consumer. Can contain wildcards.                                                     |
| `source.pedigree`          |               | The pedigree of the source broker. Used to tag outgoing messages if they originated on the source broker.            |
| `source.subscription-name` | "amqp-bridge" | The subscription name for the durable subscriber.                                                                    |
| `target.uri`               |               | The URI of the source broker. See URI format and options below.                                                      |
| `target.type`              | "topic"       | The type of the source address. Valid values: "queue", "topic"                                                       |
| `target.pedigree`          |               | The pedigree of the target broker. Used to filter/drop messages that originated on the target broker to avoid loops. |

## Qpid JMS Configuration

https://qpid.apache.org/releases/qpid-jms-2.7.0/docs/index.html
