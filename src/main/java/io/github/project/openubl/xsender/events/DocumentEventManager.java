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
package io.github.project.openubl.xsender.events;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Transactional
@ApplicationScoped
public class DocumentEventManager {

    @Inject
    Event<DocumentEvent> documentEvent;

    @Inject
    @Channel("document-event-emitter")
    @OnOverflow(value = OnOverflow.Strategy.LATEST)
    Emitter<DocumentEvent> documentEventBroadcaster;

    public void fire(DocumentEvent event) {
        documentEvent.fire(event);
    }

    protected void onDocumentEventFired(@Observes DocumentEvent event) {
        documentEventBroadcaster.send(event);
    }

}
