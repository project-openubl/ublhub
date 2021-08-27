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
package io.github.project.openubl.xsender.scheduler.impl;

import io.github.project.openubl.xsender.scheduler.Scheduler;
import io.github.project.openubl.xsender.scheduler.SchedulerProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@SchedulerProvider(SchedulerProvider.Type.VERTX)
public class VertxScheduler implements Scheduler {

    public static final String VERTX_SCHEDULER_BUS_NAME = "vertx-scheduler";

    @Inject
    EventBus eventBus;

    @Override
    public Uni<Void> sendDocumentToSUNAT(String documentId) {
        eventBus.send(VERTX_SCHEDULER_BUS_NAME, documentId);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> sendVerifyTicketAtSUNAT(String documentId) {
        return Uni.createFrom().voidItem();
    }

}
