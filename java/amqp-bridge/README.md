# AMQP Bridge

To run in dev mode:

```aiignore
mvn quarkus:dev \
  '-Dsource.address=messages.CT.#' \
  -Dsource.pedigree=spoke-02 \
  "-Dsource.uri=amqps://localhost:5671?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin&transport.trustStoreLocation=$PROJECT_ROOT/artemis/tls/client-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12&transport.verifyHost=false" \
  -Dtarget.pedigree=spoke-03 \
  "-Dtarget.uri=amqp://localhost:5674?jms.clientID=spoke-02-sender&jms.username=admin&jms.password=admin"
```

The application parameters are as follows:

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

The Qpid JMS URI parameters can be found here:

https://qpid.apache.org/releases/qpid-jms-2.7.0/docs/index.html