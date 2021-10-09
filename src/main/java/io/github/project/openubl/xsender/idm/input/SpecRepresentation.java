package io.github.project.openubl.xsender.idm.input;

import io.vertx.core.json.JsonObject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SpecRepresentation {

    @Valid
    @NotNull
    private IDGeneratorRepresentation idGenerator;

    @Valid
    private SignatureGeneratorRepresentation signature;

    @NotNull
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
