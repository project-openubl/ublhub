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
