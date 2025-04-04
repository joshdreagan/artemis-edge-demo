# AMQP Client

## Running

To run in dev mode:

```
mvn quarkus:dev \
  -Dmode=producer \
  -Daddress=messages.CT.5607 \
  "-Duri=amqp://localhost:5672?jms.clientID=amqp-client-producer&jms.username=alice&jms.password=bosco"
```

To bundle and run as a standalone app:

```
mvn package -Dquarkus.package.jar.type=uber-jar
java \
  -Dmode=producer \
  -Daddress=messages.CT.5607 \
  "-Duri=amqp://localhost:5672?jms.clientID=amqp-client-producer&jms.username=alice&jms.password=bosco" \
  -jar target/amqp-client-1.0.0-SNAPSHOT-runner.jar
```

Here's an SSL example:

```
mvn package -Dquarkus.package.jar.type=uber-jar
java \
  -Dmode=producer \
  -Daddress=messages.CT.5607 \
  "-uri=amqps://localhost:5671?jms.clientID=amqp-client-producer&jms.username=alice&jms.password=bosco&transport.trustStoreLocation=$PROJECT_ROOT/artemis/tls/client-truststore.jks&transport.trustStorePassword=password&transport.trustStoreType=PKCS12" \
  -jar target/amqp-client-1.0.0-SNAPSHOT-runner.jar
```

## Application Configuration

| Property  | Default | Description                                                                               |
|:----------|:--------|:------------------------------------------------------------------------------------------|
| `mode`    |         | The mode for the client. Valid values are "consumer", or "producer".                      |
| `uri`     |         | The URI of the broker. See URI format and options below.                                  |
| `type`    | "topic" | The type of the address. Valid values: "queue", or "topic"                                |
| `address` |         | The address for the AMQP producer or consumer. For consumers, this can contain wildcards. |

## Qpid JMS Configuration

https://qpid.apache.org/releases/qpid-jms-2.7.0/docs/index.html