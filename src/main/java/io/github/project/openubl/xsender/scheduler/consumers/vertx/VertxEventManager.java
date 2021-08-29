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
package io.github.project.openubl.xsender.scheduler.consumers.vertx;

import io.github.project.openubl.xsender.scheduler.consumers.DocumentUniTicket;
import io.github.project.openubl.xsender.scheduler.consumers.EventManagerUtils;
import io.github.project.openubl.xsender.exceptions.AbstractSendFileException;
import io.github.project.openubl.xsender.scheduler.SchedulerManager;
import io.github.project.openubl.xsender.scheduler.impl.VertxScheduler;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class VertxEventManager {

    @Inject
    EventManagerUtils eventManagerUtils;

    @Inject
    SchedulerManager schedulerManager;

    @ConsumeEvent(VertxScheduler.VERTX_SEND_FILE_SCHEDULER_BUS_NAME)
    public Uni<Void> sendFile(String documentId) {
        return eventManagerUtils.initDocumentUniSend(documentId)
                // Process file
                .chain(documentUni -> eventManagerUtils.enrichWithFileAsBytes(documentUni)
                        .chain(fileBytes -> eventManagerUtils.enrichWithFileContent(documentUni, fileBytes))
                        .chain(xmlContentModel -> eventManagerUtils.enrichWithWsConfig(documentUni, xmlContentModel))
                        .chain(wsConfig -> eventManagerUtils.enrichWithSendingFile(documentUni, wsConfig, 2))
                        .chain(billServiceModel -> eventManagerUtils.enrichSavingCDRIfExists(documentUni, billServiceModel))

                        .map(unused -> documentUni)

                        .onFailure(throwable -> throwable instanceof AbstractSendFileException)
                        .recoverWithItem(documentUni)
                )
                // Persist changes in DB
                .chain(documentUni -> eventManagerUtils.documentUniToEntity(documentUni)
                        .map(documentEntity -> documentUni)
                )

                // Final decision
                .chain(documentUni -> {
                    Uni<Void> result = Uni.createFrom().voidItem();
                    if (documentUni.getError() == null && documentUni.getBillServiceModel() != null && documentUni.getBillServiceModel().getTicket() != null) {
                        result = Uni.createFrom().emitter(uniEmitter -> {
                            schedulerManager.sendVerifyTicketAtSUNAT(documentId);
                            uniEmitter.complete(null);
                        });
                    }
                    return result;
                });
    }

    @ConsumeEvent(VertxScheduler.VERTX_CHECK_TICKET_SCHEDULER_BUS_NAME)
    public Uni<DocumentUniTicket> checkTicket(String documentId) {
        return eventManagerUtils.initDocumentUniTicket(documentId)
                // Process ticket
                .chain(documentUniTicket -> eventManagerUtils.enrichWithWsConfig(documentUniTicket)
                        .chain(() -> eventManagerUtils.enrichWithCheckingTicket(documentUniTicket, 1))
                        .chain(billServiceModel -> eventManagerUtils.enrichSavingCDRIfExists(documentUniTicket, billServiceModel))

                        .map(unused -> documentUniTicket)

                        .onFailure(throwable -> throwable instanceof AbstractSendFileException)
                        .recoverWithItem(documentUniTicket)
                )
                // Persist in DB
                .chain(documentUni -> eventManagerUtils.documentUniToEntity(documentUni)
                        .map(documentEntity -> documentUni)
                );
    }
}
