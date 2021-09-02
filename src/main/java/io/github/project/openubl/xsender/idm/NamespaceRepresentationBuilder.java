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
package io.github.project.openubl.xsender.idm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public final class NamespaceRepresentationBuilder {
    private String id;
    private String name;
    private String description;

    private NamespaceRepresentationBuilder() {
    }

    public static NamespaceRepresentationBuilder aNamespaceRepresentation() {
        return new NamespaceRepresentationBuilder();
    }

    public NamespaceRepresentationBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public NamespaceRepresentationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public NamespaceRepresentationBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public NamespaceRepresentation build() {
        NamespaceRepresentation namespaceRepresentation = new NamespaceRepresentation();
        namespaceRepresentation.setId(id);
        namespaceRepresentation.setName(name);
        namespaceRepresentation.setDescription(description);
        return namespaceRepresentation;
    }
}
