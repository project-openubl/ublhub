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
package io.github.project.openubl.ublhub.idm.input;

import io.github.project.openubl.ublhub.idgenerator.IDGeneratorType;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class IDGeneratorRepresentation {

    @NotNull
    private IDGeneratorType name;

    private Map<String, String> config;

    public IDGeneratorType getName() {
        return name;
    }

    public void setName(IDGeneratorType name) {
        this.name = name;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public static final class Builder {
        private IDGeneratorType name;
        private Map<String, String> config;

        private Builder() {
        }

        public static Builder anIDGeneratorRepresentation() {
            return new Builder();
        }

        public Builder withName(IDGeneratorType name) {
            this.name = name;
            return this;
        }

        public Builder withConfig(Map<String, String> config) {
            this.config = config;
            return this;
        }

        public IDGeneratorRepresentation build() {
            IDGeneratorRepresentation iDGeneratorRepresentation = new IDGeneratorRepresentation();
            iDGeneratorRepresentation.setName(name);
            iDGeneratorRepresentation.setConfig(config);
            return iDGeneratorRepresentation;
        }
    }
}
