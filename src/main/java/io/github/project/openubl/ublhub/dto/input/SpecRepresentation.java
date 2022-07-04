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
package io.github.project.openubl.ublhub.dto.input;

import io.vertx.core.json.JsonObject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SpecRepresentation {

    @Valid
    @NotNull
    private IDGeneratorRepresentation idGenerator;

    @Valid
    private SignatureGeneratorRepresentation signature;

    private JsonObject document;

    public IDGeneratorRepresentation getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IDGeneratorRepresentation idGenerator) {
        this.idGenerator = idGenerator;
    }

    public SignatureGeneratorRepresentation getSignature() {
        return signature;
    }

    public void setSignature(SignatureGeneratorRepresentation signature) {
        this.signature = signature;
    }

    public JsonObject getDocument() {
        return document;
    }

    public void setDocument(JsonObject document) {
        this.document = document;
    }


    public static final class Builder {
        private IDGeneratorRepresentation idGenerator;
        private SignatureGeneratorRepresentation signature;
        private JsonObject document;

        private Builder() {
        }

        public static Builder aSpecRepresentation() {
            return new Builder();
        }

        public Builder withIdGenerator(IDGeneratorRepresentation idGenerator) {
            this.idGenerator = idGenerator;
            return this;
        }

        public Builder withSignature(SignatureGeneratorRepresentation signature) {
            this.signature = signature;
            return this;
        }

        public Builder withDocument(JsonObject document) {
            this.document = document;
            return this;
        }

        public SpecRepresentation build() {
            SpecRepresentation specRepresentation = new SpecRepresentation();
            specRepresentation.setIdGenerator(idGenerator);
            specRepresentation.setSignature(signature);
            specRepresentation.setDocument(document);
            return specRepresentation;
        }
    }
}
