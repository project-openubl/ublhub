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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.ublhub.ubl.content.enricher.InvoiceEnricher;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.renderer.InvoiceRenderer;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/carlos")
public class Carlos {

    @Inject
    InvoiceEnricher invoiceEnricher;

    @Inject
    InvoiceRenderer invoiceRenderer;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public BoletaFactura executeQuery(BoletaFactura invoiceDto) {
        return invoiceEnricher.enrich(invoiceDto);
    }

    @PUT
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> createXML(BoletaFactura invoiceDto) {
        BoletaFactura enrichedInvoice = executeQuery(invoiceDto);
        return invoiceRenderer.renderInvoice(enrichedInvoice);
    }

}
