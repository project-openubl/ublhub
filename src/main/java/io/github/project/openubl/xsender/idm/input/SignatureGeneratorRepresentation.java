package io.github.project.openubl.xsender.idm.input;

import javax.validation.constraints.NotNull;

public class SignatureGeneratorRepresentation {

    @NotNull
    private String algorithm;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public static final class Builder {
        private String algorithm;

        private Builder() {
        }

        public static Builder aSignatureGeneratorRepresentation() {
            return new Builder();
        }

        public Builder withAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public SignatureGeneratorRepresentation build() {
            SignatureGeneratorRepresentation signatureGeneratorRepresentation = new SignatureGeneratorRepresentation();
            signatureGeneratorRepresentation.setAlgorithm(algorithm);
            return signatureGeneratorRepresentation;
        }
    }
}
