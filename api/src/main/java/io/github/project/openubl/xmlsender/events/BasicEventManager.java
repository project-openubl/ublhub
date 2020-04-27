/**
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
package io.github.project.openubl.xmlsender.events;

import io.github.project.openubl.xmlsender.events.jms.SendTicketQueueConsumer;
import io.github.project.openubl.xmlsender.idm.DocumentRepresentation;
import io.github.project.openubl.xmlsender.models.DocumentEvent;
import io.github.project.openubl.xmlsender.models.jpa.DocumentRepository;
import io.github.project.openubl.xmlsender.models.jpa.entities.DocumentEntity;
import io.github.project.openubl.xmlsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xmlsender.resources.client.CallbackClientService;
import io.github.project.openubl.xmlsender.ws.WSSunatClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;

@ApplicationScoped
public class BasicEventManager {

    private static final Logger LOG = Logger.getLogger(BasicEventManager.class);

    @Inject
    WSSunatClient wsSunatClient;

    @Inject
    @RestClient
    CallbackClientService callbackClientService;

    @Inject
    DocumentRepository documentRepository;

    public void onDocumentCreate(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            @EventProvider(EventProvider.Type.basic) DocumentEvent.Created event
    ) {
        wsSunatClient.sendDocument(event.getId());
    }

    public void onDocumentRequireCheckTicket(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            @EventProvider(EventProvider.Type.basic) DocumentEvent.RequireCheckTicket event
    ) {
        wsSunatClient.checkDocumentTicket(event.getId());
    }

    @Transactional
    public void onDocumentDelivered(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            @EventProvider(EventProvider.Type.basic) DocumentEvent.Delivered event
    ) {
        DocumentEntity documentEntity = documentRepository.findById(event.getId());
        DocumentRepresentation rep = EntityToRepresentation.toRepresentation(documentEntity);
        try {
            callbackClientService.callback(rep);
        } catch (WebApplicationException e) {
            LOG.error("Could not send webhook callback, message=" + e.getMessage());
        }
    }

}
