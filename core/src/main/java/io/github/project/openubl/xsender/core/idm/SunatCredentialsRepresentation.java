/**
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
package io.github.project.openubl.xsender.core.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RegisterForReflection
public class SunatCredentialsRepresentation {

    @NotNull
    @Size(min = 3, max = 250)
    private String sunatUsername;

    @NotNull
    @Size(min = 3, max = 250)
    private String sunatPassword;

    public String getSunatUsername() {
        return sunatUsername;
    }

    public void setSunatUsername(String sunatUsername) {
        this.sunatUsername = sunatUsername;
    }

    public String getSunatPassword() {
        return sunatPassword;
    }

    public void setSunatPassword(String sunatPassword) {
        this.sunatPassword = sunatPassword;
    }

    public static final class Builder {
        private String sunatUsername;
        private String sunatPassword;

        private Builder() {
        }

        public static Builder aSunatCredentialsRepresentation() {
            return new Builder();
        }

        public Builder withSunatUsername(String sunatUsername) {
            this.sunatUsername = sunatUsername;
            return this;
        }

        public Builder withSunatPassword(String sunatPassword) {
            this.sunatPassword = sunatPassword;
            return this;
        }

        public SunatCredentialsRepresentation build() {
            SunatCredentialsRepresentation sunatCredentialsRepresentation = new SunatCredentialsRepresentation();
            sunatCredentialsRepresentation.setSunatUsername(sunatUsername);
            sunatCredentialsRepresentation.setSunatPassword(sunatPassword);
            return sunatCredentialsRepresentation;
        }
    }
}
