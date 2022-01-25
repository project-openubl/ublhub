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
package io.github.project.openubl.ublhub.ubl.sender;

public final class XMLSenderConfigBuilder {

    private String facturaUrl;
    private String guiaRemisionUrl;
    private String percepcionRetencionUrl;
    private String username;
    private String password;

    private XMLSenderConfigBuilder() {
    }

    public static XMLSenderConfigBuilder aXMLSenderConfig() {
        return new XMLSenderConfigBuilder();
    }

    public XMLSenderConfigBuilder withFacturaUrl(String facturaUrl) {
        this.facturaUrl = facturaUrl;
        return this;
    }

    public XMLSenderConfigBuilder withGuiaRemisionUrl(String guiaRemisionUrl) {
        this.guiaRemisionUrl = guiaRemisionUrl;
        return this;
    }

    public XMLSenderConfigBuilder withPercepcionRetencionUrl(String percepcionRetencionUrl) {
        this.percepcionRetencionUrl = percepcionRetencionUrl;
        return this;
    }

    public XMLSenderConfigBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public XMLSenderConfigBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public XMLSenderConfig build() {
        XMLSenderConfig xMLSenderConfig = new XMLSenderConfig();
        xMLSenderConfig.setFacturaUrl(facturaUrl);
        xMLSenderConfig.setGuiaRemisionUrl(guiaRemisionUrl);
        xMLSenderConfig.setPercepcionRetencionUrl(percepcionRetencionUrl);
        xMLSenderConfig.setUsername(username);
        xMLSenderConfig.setPassword(password);
        return xMLSenderConfig;
    }
}