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
package io.github.project.openubl.ublhub.ubl.renderer;

import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.ubl.content.models.common.Cliente;
import io.github.project.openubl.ublhub.ubl.content.models.common.Proveedor;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog6;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static io.github.project.openubl.ublhub.ubl.renderer.RendererUtils.assertSendSunat;
import static io.github.project.openubl.ublhub.ubl.renderer.RendererUtils.assertSnapshot;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class XMLRendererTest {

    @Inject
    XMLRenderer xmlRenderer;

    @Test
    public void testInvoiceWithCustomUnidadMedida() throws Exception {
        // Given
        BoletaFactura input = new BoletaFactura();
        input.serie = "F001";
        input.numero = 1;
        input.proveedor = new Proveedor();
        input.proveedor.ruc = "12345678912";
        input.proveedor.razonSocial = "Softgreen S.A.C.";
        input.cliente = new Cliente();
        input.cliente.nombre = "Carlos Feria";
        input.cliente.numeroDocumentoIdentidad = "12121212121";
        input.cliente.tipoDocumentoIdentidad = Catalog6.RUC.toString();

        DocumentoDetalle detalle1 = new DocumentoDetalle();
        detalle1.descripcion = "Item1";
        detalle1.cantidad = new BigDecimal(10);
        detalle1.precio = new BigDecimal(100);
        detalle1.unidadMedida = "KGM";

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);
        detalle2.unidadMedida = "KGM";

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlRenderer.renderInvoice(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, "xml/invoice/customUnidadMedida.xml");
        assertSendSunat(xml);
    }

}