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

import io.github.project.openubl.ublhub.scheduler.Scheduler;
import io.github.project.openubl.ublhub.scheduler.SchedulerProvider;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.apache.camel.ProducerTemplate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
@SchedulerProvider(SchedulerProvider.Type.AMQP)
public class JmsScheduler implements Scheduler {

    @Inject
    ProducerTemplate producerTemplate;

    @Override
    public Uni<Void> sendDocumentToSUNAT(String documentId) {
        producerTemplate.requestBody("direct:schedule-send-document", documentId);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> sendVerifyTicketAtSUNAT(String documentId) {
        return Uni.createFrom().voidItem();
    }

}
