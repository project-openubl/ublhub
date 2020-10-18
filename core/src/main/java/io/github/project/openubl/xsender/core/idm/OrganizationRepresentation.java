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

@RegisterForReflection
public class OrganizationRepresentation {

    @NotNull
    private String name;
    private String description;

    private SunatUrlsRepresentation sunatUrls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SunatUrlsRepresentation getSunatUrls() {
        return sunatUrls;
    }

    public void setSunatUrls(SunatUrlsRepresentation sunatUrls) {
        this.sunatUrls = sunatUrls;
    }

    public static final class Builder {
        private String name;
        private String description;
        private SunatUrlsRepresentation sunatUrls;

        private Builder() {
        }

        public static Builder aCorporateRepresentation() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withSunatUrls(SunatUrlsRepresentation sunatUrls) {
            this.sunatUrls = sunatUrls;
            return this;
        }

        public OrganizationRepresentation build() {
            OrganizationRepresentation corporateRepresentation = new OrganizationRepresentation();
            corporateRepresentation.setName(name);
            corporateRepresentation.setDescription(description);
            corporateRepresentation.setSunatUrls(sunatUrls);
            return corporateRepresentation;
        }
    }
}
