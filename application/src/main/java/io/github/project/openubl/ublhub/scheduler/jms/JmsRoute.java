/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.scheduler.jms;

import io.github.project.openubl.ublhub.scheduler.vertx.VertxScheduler;
import io.vertx.core.eventbus.EventBus;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;

@ApplicationScoped
public class JmsRoute extends RouteBuilder {

    @ConfigProperty(name = "amqp-host")
    String amqpHost;

    @ConfigProperty(name = "amqp-port")
    String amqpPort;

    @ConfigProperty(name = "amqp-user")
    String amqpUsername;

    @ConfigProperty(name = "amqp-password")
    String amqpPassword;

    @Inject
    EventBus eventBus;

    @Produces
    @Named("connectionFactory")
    public ConnectionFactory connectionFactory() throws Exception {
        String url = "amqp://" + amqpHost + ":" + amqpPort;
        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(amqpUsername, amqpPassword, url);
        return connectionFactory;
    }

    @Override
    public void configure() throws Exception {
        from("direct:schedule-send-document")
                .id("in-schedule-send-document")
                .to(ExchangePattern.InOnly, "jms:queue:schedule-send-document?jmsMessageType=Text");

        from("jms:queue:schedule-send-document?jmsMessageType=Text")
                .id("out-schedule-send-document")
                .process(exchange -> {
                    eventBus.send(VertxScheduler.VERTX_SEND_FILE_SCHEDULER_BUS_NAME, exchange.getIn().getBody());
                });
    }

}
