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
package io.github.project.openubl.ublhub.models.jpa.entities.listeners;

import io.github.project.openubl.ublhub.events.BroadcasterEventManager;
import io.github.project.openubl.ublhub.idm.DocumentRepresentation;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

public class UBLDocumentEntityListener {

    @PostPersist
    @PostUpdate
    private void afterAnyUpdate(UBLDocumentEntity document) {
        DocumentRepresentation documentRepresentation = EntityToRepresentation.toRepresentation(document);

        BroadcasterEventManager broadcasterEventManager = CDI.current().select(BroadcasterEventManager.class).get();
        broadcasterEventManager.broadcast(documentRepresentation);
    }

}
