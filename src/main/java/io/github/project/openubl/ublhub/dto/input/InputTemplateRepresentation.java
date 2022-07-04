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

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RegisterForReflection
public class InputTemplateRepresentation {

    @NotNull
    private KindRepresentation kind;

    @Valid
    private MetadataRepresentation metadata;

    @Valid
    @NotNull
    private SpecRepresentation spec;

    public KindRepresentation getKind() {
        return kind;
    }

    public void setKind(KindRepresentation kind) {
        this.kind = kind;
    }

    public MetadataRepresentation getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataRepresentation metadata) {
        this.metadata = metadata;
    }

    public SpecRepresentation getSpec() {
        return spec;
    }

    public void setSpec(SpecRepresentation spec) {
        this.spec = spec;
    }

    public static final class Builder {
        private KindRepresentation kind;
        private MetadataRepresentation metadata;
        private SpecRepresentation spec;

        private Builder() {
        }

        public static Builder anInputTemplateRepresentation() {
            return new Builder();
        }

        public Builder withKind(KindRepresentation kind) {
            this.kind = kind;
            return this;
        }

        public Builder withMetadata(MetadataRepresentation metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withSpec(SpecRepresentation spec) {
            this.spec = spec;
            return this;
        }

        public InputTemplateRepresentation build() {
            InputTemplateRepresentation inputTemplateRepresentation = new InputTemplateRepresentation();
            inputTemplateRepresentation.setKind(kind);
            inputTemplateRepresentation.setMetadata(metadata);
            inputTemplateRepresentation.setSpec(spec);
            return inputTemplateRepresentation;
        }
    }
}
