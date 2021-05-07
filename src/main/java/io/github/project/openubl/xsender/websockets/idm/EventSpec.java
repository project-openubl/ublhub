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
package io.github.project.openubl.xsender.websockets.idm;

import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;

public class EventSpec {

    public String id;
    public EventType event;
    public EntityType entity;

    public static final class Builder {
        public String id;
        public EventType event;
        public EntityType entity;

        private Builder() {
        }

        public static Builder anEventSpec() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withEvent(EventType event) {
            this.event = event;
            return this;
        }

        public Builder withEntity(EntityType entity) {
            this.entity = entity;
            return this;
        }

        public EventSpec build() {
            EventSpec eventSpec = new EventSpec();
            eventSpec.event = this.event;
            eventSpec.entity = this.entity;
            eventSpec.id = this.id;
            return eventSpec;
        }
    }
}
