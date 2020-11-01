/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * <p>
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.eclipse.org/legal/epl-2.0/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.core.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RegisterForReflection
public class CompanyRepresentation {

    @NotNull
    private String name;

    @NotNull
    @Valid
    private SunatUrlsRepresentation sunatWsUrls;

    @NotNull
    @Valid
    private SunatCredentialsRepresentation sunatCredentials;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SunatUrlsRepresentation getSunatWsUrls() {
        return sunatWsUrls;
    }

    public void setSunatWsUrls(SunatUrlsRepresentation sunatWsUrls) {
        this.sunatWsUrls = sunatWsUrls;
    }

    public SunatCredentialsRepresentation getSunatCredentials() {
        return sunatCredentials;
    }

    public void setSunatCredentials(SunatCredentialsRepresentation sunatCredentials) {
        this.sunatCredentials = sunatCredentials;
    }

    public static final class Builder {
        private String name;
        private SunatUrlsRepresentation sunatWsUrls;
        private SunatCredentialsRepresentation sunatCredentials;

        private Builder() {
        }

        public static Builder aCompanyRepresentation() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSunatWsUrls(SunatUrlsRepresentation sunatWsUrls) {
            this.sunatWsUrls = sunatWsUrls;
            return this;
        }

        public Builder withSunatCredentials(SunatCredentialsRepresentation sunatCredentials) {
            this.sunatCredentials = sunatCredentials;
            return this;
        }

        public CompanyRepresentation build() {
            CompanyRepresentation companyRepresentation = new CompanyRepresentation();
            companyRepresentation.setName(name);
            companyRepresentation.setSunatWsUrls(sunatWsUrls);
            companyRepresentation.setSunatCredentials(sunatCredentials);
            return companyRepresentation;
        }
    }
}
