package com.redhat.examples.amqp.bridge;

import com.redhat.examples.amqp.bridge.config.SourceConfiguration;
import com.redhat.examples.amqp.bridge.config.TargetConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.jms.ConnectionFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.qpid.jms.JmsConnectionFactory;

@ApplicationScoped
public class CamelConfiguration extends RouteBuilder {

  @Produces
  @Named("sourceConnectionFactory")
  ConnectionFactory sourceConnectionFactory(SourceConfiguration source) {
    JmsConnectionFactory connectionFactory = new JmsConnectionFactory();
    connectionFactory.setRemoteURI(source.uri().toString());
    return connectionFactory;
  }

  @Produces
  @Named("targetConnectionFactory")
  ConnectionFactory targetConnectionFactory(TargetConfiguration target) {
    JmsConnectionFactory connectionFactory = new JmsConnectionFactory();
    connectionFactory.setRemoteURI(target.uri().toString());
    return connectionFactory;
  }

  @Override
  public void configure() throws Exception {

    //@formatter:off
    from("amqp:{{source.type:topic}}:{{source.address}}?connectionFactory=#sourceConnectionFactory&subscriptionDurable=true&subscriptionName={{source.subscription-name}}")
      .routeId("amqp-consumer")
      .log(LoggingLevel.INFO, "Received message: ${header.JMSMessageID}")
      .log(LoggingLevel.TRACE, "${headers}: ${body}")
      .filter(simple("${header.Pedigree} == ${properties:target.pedigree}"))
        .log(LoggingLevel.INFO, "Dropping message: ${header.JMSMessageID}")
        .stop()
      .end()
      .filter(simple("${header.Pedigree} == ${null} || ${header.Pedigree.isBlank()}"))
        .setHeader("Pedigree", simple("${properties:source.pedigree}"))
      .end()
      .log(LoggingLevel.INFO, "Sending message: ${header.JMSMessageID}")
      .toD("amqp:{{source.type:topic}}:${header.JMSDestination}?connectionFactory=#targetConnectionFactory&disableReplyTo=true")
    ;
    //@formatter:on
  }
}
