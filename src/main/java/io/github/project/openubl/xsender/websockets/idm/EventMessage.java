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

public class EventMessage {

    public TypeMessage type;
    public EventSpec spec;

    public static final class Builder {
        public TypeMessage type;
        public EventSpec spec;

        private Builder() {
        }

        public static Builder anEventMessage() {
            return new Builder();
        }

        public Builder withType(TypeMessage type) {
            this.type = type;
            return this;
        }

        public Builder withSpec(EventSpec spec) {
            this.spec = spec;
            return this;
        }

        public EventMessage build() {
            EventMessage eventMessage = new EventMessage();
            eventMessage.type = this.type;
            eventMessage.spec = this.spec;
            return eventMessage;
        }
    }
}
