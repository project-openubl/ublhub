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

import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceLineUnit;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceTotalImpuestosUnit;
import io.github.project.openubl.ublhub.ubl.content.ruleunits.InvoiceUnit;
import org.kie.kogito.incubation.application.AppRoot;
import org.kie.kogito.incubation.rules.QueryId;
import org.kie.kogito.incubation.rules.services.RuleUnitService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.rules.RuleUnitIds;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/carlos")
public class Carlos {

    @Inject
    AppRoot appRoot;

    @Inject
    RuleUnitService svc;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public BoletaFactura executeQuery(BoletaFactura invoiceDto) {
        // General
        QueryId invoiceQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceUnit.class).queries().get("boletaFactura");
        DataContext invoiceCtx = MapDataContext.of(Map.of("invoice", invoiceDto));
        Stream<BoletaFactura> invoiceStream = svc.evaluate(invoiceQueryId, invoiceCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        BoletaFactura invoice = invoiceStream.findFirst().orElseThrow(IllegalStateException::new);

        // Lines
        QueryId invoiceLineQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceLineUnit.class).queries().get("detalle");
        MapDataContext invoiceLineCtx = MapDataContext.of(Map.of("invoiceLines", new LinkedList<>(invoiceDto.detalle)));
        Stream<DocumentoDetalle> invoiceLineStream = svc.evaluate(invoiceLineQueryId, invoiceLineCtx).map(dc -> dc.as(MapDataContext.class).get("$il", DocumentoDetalle.class));

        invoice.detalle = invoiceLineStream.collect(Collectors.toList());

        // Totals
        invoiceQueryId = appRoot.get(RuleUnitIds.class).get(InvoiceTotalImpuestosUnit.class).queries().get("boletaFacturaTotalImpuestos");
        invoiceCtx = MapDataContext.of(Map.of("invoice", invoice));
        invoiceStream = svc.evaluate(invoiceQueryId, invoiceCtx).map(dc -> dc.as(MapDataContext.class).get("$i", BoletaFactura.class));

        invoice = invoiceStream.findFirst().orElseThrow(IllegalStateException::new);

        // Result
        return invoice;
    }

}
