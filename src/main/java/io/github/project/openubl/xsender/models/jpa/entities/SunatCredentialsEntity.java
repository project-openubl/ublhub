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
package io.github.project.openubl.xsender.models.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class SunatCredentialsEntity {

    @NotNull
    @Column(name = "sunat_username")
    private String sunatUsername;

    @NotNull
    @Column(name = "sunat_password")
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

        public static Builder aSunatCredentialsEntity() {
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

        public SunatCredentialsEntity build() {
            SunatCredentialsEntity sunatCredentialsEntity = new SunatCredentialsEntity();
            sunatCredentialsEntity.setSunatUsername(sunatUsername);
            sunatCredentialsEntity.setSunatPassword(sunatPassword);
            return sunatCredentialsEntity;
        }
    }
}
