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
package io.github.project.openubl.ublhub.messaging;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

@ApplicationScoped
public class JmsMessagesRoute extends RouteBuilder {

    @ConfigProperty(name = "openubl.messaging.type")
    String schedulerType;

    @ConfigProperty(name = "openubl.messaging.jsm.queue")
    String jmsQueue;

    @Inject
    Instance<ConnectionFactory> connectionFactory;

    @BindToRegistry("connectionFactory")
    public ConnectionFactory connectionFactory() {
        if (connectionFactory.isResolvable()) {
            return connectionFactory.get();
        } else {
            return new ActiveMQJMSConnectionFactory();
        }
    }

    @Override
    public void configure() throws Exception {
        from("jms:queue:" + jmsQueue + "?connectionFactory=#connectionFactory")
                .id("jms-send-xml")
                .precondition(String.valueOf(schedulerType.equalsIgnoreCase("jms")))
                .to("direct:send-xml");
    }

}
