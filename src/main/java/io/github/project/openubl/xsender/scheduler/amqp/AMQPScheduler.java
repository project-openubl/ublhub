/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.scheduler.amqp;

import io.github.project.openubl.xsender.scheduler.Scheduler;
import io.github.project.openubl.xsender.scheduler.SchedulerProvider;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@SchedulerProvider(SchedulerProvider.Type.AMQP)
public class AMQPScheduler implements Scheduler {

    @Inject
    @Channel("send-document-sunat-emitter")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    Emitter<String> documentEmitter;

    @Override
    public Uni<Void> sendDocumentToSUNAT(String documentId) {
        OutgoingAmqpMetadata outgoingAmqpMetadata = OutgoingAmqpMetadata.builder()
                .withMessageAnnotations("x-opt-delivery-delay", 200)
                .build();
        Message<String> scheduledMessage = Message
                .of(documentId)
                .withMetadata(Metadata.of(outgoingAmqpMetadata));
        documentEmitter.send(scheduledMessage);

        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> sendVerifyTicketAtSUNAT(String documentId) {
        // This is not used since the way is with the Consumer
        return Uni.createFrom().voidItem();
    }

}
