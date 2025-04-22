# Artemis Extensions

Build the extensions jar:

```
mvn clean package
```

Copy the jar into your broker instance lib directory:

```
cp ./target/artemis-extensions-*.jar ${ARTEMIS_INSTANCE}/lib/
```

Add the following snippet to your "${ARTEMIS_INSTANCE}/etc/broker.xml" file anywhere under the `<core>` element:

```
<broker-plugins>
  <broker-plugin class-name="com.redhat.examples.artemis.core.StaticHeaderPlugin">
    <property key="HEADER_NAME" value="Pedigree"/>
    <property key="HEADER_VALUE" value="spoke-01"/>
  </broker-plugin>
</broker-plugins>
```
