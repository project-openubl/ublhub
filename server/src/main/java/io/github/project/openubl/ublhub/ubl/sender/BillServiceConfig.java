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

import io.github.project.openubl.xmlsenderws.webservices.managers.smart.custom.CustomBillServiceConfig;

public class BillServiceConfig implements CustomBillServiceConfig {

    private final String facturaUrl;
    private final String guiaRemisionUrl;
    private final String percepcionRetencionUrl;

    public BillServiceConfig(String facturaUrl, String guiaRemisionUrl, String percepcionRetencionUrl) {
        this.facturaUrl = facturaUrl;
        this.guiaRemisionUrl = guiaRemisionUrl;
        this.percepcionRetencionUrl = percepcionRetencionUrl;
    }

    @Override
    public String getInvoiceAndNoteDeliveryURL() {
        return facturaUrl;
    }

    @Override
    public String getDespatchAdviceDeliveryURL() {
        return guiaRemisionUrl;
    }

    @Override
    public String getPerceptionAndRetentionDeliveryURL() {
        return percepcionRetencionUrl;
    }
}
