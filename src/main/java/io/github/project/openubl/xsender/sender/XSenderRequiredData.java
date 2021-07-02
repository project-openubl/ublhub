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
package io.github.project.openubl.xsender.sender;

import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;

public class XSenderRequiredData {

    private SunatUrlsEntity urls;
    private SunatCredentialsEntity credentials;

    public XSenderRequiredData(SunatUrlsEntity urls, SunatCredentialsEntity credentials) {
        this.urls = urls;
        this.credentials = credentials;
    }

    public SunatUrlsEntity getUrls() {
        return urls;
    }

    public void setUrls(SunatUrlsEntity urls) {
        this.urls = urls;
    }

    public SunatCredentialsEntity getCredentials() {
        return credentials;
    }

    public void setCredentials(SunatCredentialsEntity credentials) {
        this.credentials = credentials;
    }
}
