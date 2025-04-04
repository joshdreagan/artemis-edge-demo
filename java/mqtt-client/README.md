# MQTT Client

## Running

To run in dev mode:

```
mvn quarkus:dev \
  -Dmode=producer \
  '-Daddress=messages/CT/5607' \
  -Dmqtt.client-id=mqtt-client-producer \
  '-Dmqtt.broker-url=tcp://localhost:1883' \
  -Dmqtt.user-name=alice \
  -Dmqtt.password=bosco
```

To bundle and run as a standalone app:

```
mvn package -Dquarkus.package.jar.type=uber-jar
java \
  -Dmode=producer \
  '-Daddress=messages/CT/5607' \
  -Dmqtt.client-id=mqtt-client-producer \
  '-Dmqtt.broker-url=tcp://localhost:1883' \
  -Dmqtt.user-name=alice \
  -Dmqtt.password=bosco \
  -jar target/mqtt-client-1.0.0-SNAPSHOT-runner.jar
```

Here's an SSL example:

```
java \
  -Dmode=producer \
  '-Daddress=messages/CT/5607' \
  -Dmqtt.client-id=mqtt-client-producer \
  -Dmqtt.broker-url=ssl://hub-01-broker-mqtts-acceptor-0-svc-rte-hub-01.apps.cluster-5qtbx.5qtbx.sandbox1380.opentlc.com:443 \
  -Dmqtt.user-name=alice \
  -Dmqtt.password=bosco \
  -Dmqtt.ssl-client-props."com.ibm.ssl.trustStore"=$PROJECT_ROOT/artemis/tls/client-truststore.jks \
  -Dmqtt.ssl-client-props."com.ibm.ssl.trustStorePassword"=password \
  -Dmqtt.ssl-client-props."com.ibm.ssl.trustStoreType"=PKCS12  \
  -jar target/mqtt-client-1.0.0-SNAPSHOT-runner.jar
```

## Application Configuration

| Property  | Default | Description                                                                                                                                                                                                                                                                                                                          |
|:----------|:--------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `mode`    |         | The mode for the client. Valid values are "consumer", or "producer".                                                                                                                                                                                                                                                                 |
| `address` |         | The address for the MQTT producer or consumer. For consumers, this can contain wildcards.                                                                                                                                                                                                                                            |
| `mqtt.*`  |         | The properties for the MQTT client connection. These can include any valid configurations from the Paho MQTT5 Camel Component linked below, prefixed with "mqtt.", and using the Quarkus property naming convention. For instance, to set the `clientId` component configuration, you would use the property named `mqtt.client-id`. |

## Paho MQTT5 Component Configuration

https://camel.apache.org/components/4.10.x/paho-mqtt5-component.html
