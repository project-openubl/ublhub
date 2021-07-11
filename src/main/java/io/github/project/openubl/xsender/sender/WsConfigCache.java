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

public class WsConfigCache {
    private String facturaUrl;
    private String guiaUrl;
    private String percepcionRetencionUrl;

    private String username;
    private String password;

    public String getFacturaUrl() {
        return facturaUrl;
    }

    public void setFacturaUrl(String facturaUrl) {
        this.facturaUrl = facturaUrl;
    }

    public String getGuiaUrl() {
        return guiaUrl;
    }

    public void setGuiaUrl(String guiaUrl) {
        this.guiaUrl = guiaUrl;
    }

    public String getPercepcionRetencionUrl() {
        return percepcionRetencionUrl;
    }

    public void setPercepcionRetencionUrl(String percepcionRetencionUrl) {
        this.percepcionRetencionUrl = percepcionRetencionUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
