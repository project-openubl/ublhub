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
package io.github.project.openubl.ublhub.events;

import io.github.project.openubl.ublhub.idm.DocumentRepresentation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

@ApplicationScoped
public class BroadcasterEventManager {

    @ConfigProperty(name = "openubl.scheduler.type")
    String schedulerType;

    @Inject
    @Any
    Instance<BroadcasterEvent> broadcasters;

    public void broadcast(DocumentRepresentation rep) {
        BroadcasterEventProvider.Type providerType = BroadcasterEventProvider.Type.valueOf(schedulerType.toUpperCase());
        Annotation annotation = new BroadcasterEventProviderLiteral(providerType);

        BroadcasterEvent broadcaster = broadcasters.select(annotation).get();
        broadcaster.broadcast(rep);
    }

}
