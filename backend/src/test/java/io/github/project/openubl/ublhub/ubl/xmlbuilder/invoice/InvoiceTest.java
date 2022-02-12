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
package io.github.project.openubl.ublhub.ubl.xmlbuilder.invoice;

import io.github.project.openubl.ublhub.ProfileManager;
import io.github.project.openubl.ublhub.ubl.XMLBuilder;
import io.github.project.openubl.ublhub.ubl.content.models.common.*;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.BoletaFactura;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DocumentoDetalle;
import io.github.project.openubl.xmlbuilderlib.models.catalogs.Catalog6;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;

import static io.github.project.openubl.ublhub.ubl.xmlbuilder.XMLAssertUtils.assertSendSunat;
import static io.github.project.openubl.ublhub.ubl.xmlbuilder.XMLAssertUtils.assertSnapshot;

@QuarkusTest
@TestProfile(ProfileManager.class)
public class InvoiceTest {

    @Inject
    XMLBuilder xmlBuilder;

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
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();

        assertSnapshot(xml, getClass(), "customUnidadMedida.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithCustomFechaEmision() throws Exception {
        LocalDate fechaEmision = LocalDate.of(2019, Month.JANUARY, 6);
        LocalTime horaEmision = LocalTime.of(0, 0);

        // Given
        BoletaFactura input = new BoletaFactura();
        input.serie = "F001";
        input.numero = 1;
        input.fechaEmision = fechaEmision;
        input.horaEmision = horaEmision;

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

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "customFechaEmision.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithCustomClienteDireccionAndContacto() throws Exception {
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
        input.cliente.contacto = new Contacto();
        input.cliente.contacto.email = "carlos@gmail.com";
        input.cliente.contacto.telefono = "+123456789";
        input.cliente.direccion = new Direccion();
        input.cliente.direccion.ubigeo = "050101";
        input.cliente.direccion.departamento = "Ayacucho";
        input.cliente.direccion.provincia = "Huamanga";
        input.cliente.direccion.distrito = "Jesus Nazareno";
        input.cliente.direccion.codigoLocal = "0101";
        input.cliente.direccion.urbanizacion = "000000";
        input.cliente.direccion.direccion = "Jr. Las piedras 123";
        input.cliente.direccion.codigoPais = "PE";

        DocumentoDetalle detalle1 = new DocumentoDetalle();
        detalle1.descripcion = "Item1";
        detalle1.cantidad = new BigDecimal(10);
        detalle1.precio = new BigDecimal(100);

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "customClienteDireccionAndContacto.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithCustomProveedorDireccionAndContacto() throws Exception {
        // Given
        BoletaFactura input = new BoletaFactura();
        input.serie = "F001";
        input.numero = 1;

        input.proveedor = new Proveedor();
        input.proveedor.ruc = "12345678912";
        input.proveedor.razonSocial = "Softgreen S.A.C.";
        input.proveedor.contacto = new Contacto();
        input.proveedor.contacto.email = "carlos@gmail.com";
        input.proveedor.contacto.telefono = "+123456789";
        input.proveedor.direccion = new Direccion();
        input.proveedor.direccion.ubigeo = "050101";
        input.proveedor.direccion.departamento = "Ayacucho";
        input.proveedor.direccion.provincia = "Huamanga";
        input.proveedor.direccion.distrito = "Jesus Nazareno";
        input.proveedor.direccion.codigoLocal = "0101";
        input.proveedor.direccion.urbanizacion = "000000";
        input.proveedor.direccion.direccion = "Jr. Las piedras 123";
        input.proveedor.direccion.codigoPais = "PE";

        input.cliente = new Cliente();
        input.cliente.nombre = "Carlos Feria";
        input.cliente.numeroDocumentoIdentidad = "12121212121";
        input.cliente.tipoDocumentoIdentidad = Catalog6.RUC.toString();

        DocumentoDetalle detalle1 = new DocumentoDetalle();
        detalle1.descripcion = "Item1";
        detalle1.cantidad = new BigDecimal(10);
        detalle1.precio = new BigDecimal(100);

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "customProveedorDireccionAndContacto.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithCustomFirmante() throws Exception {
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

        input.firmante = new Firmante();
        input.firmante.ruc = "000000000000";
        input.firmante.razonSocial = "Wolsnut4 S.A.C.";

        DocumentoDetalle detalle1 = new DocumentoDetalle();
        detalle1.descripcion = "Item1";
        detalle1.cantidad = new BigDecimal(10);
        detalle1.precio = new BigDecimal(100);

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "customFirmante.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithICB_precioUnitario() throws Exception {
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
        detalle1.icbAplica = true;

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(100);
        detalle2.icbAplica = true;

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "icb.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithICB_precioConIgv() throws Exception {
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
        detalle1.precio = new BigDecimal(118);
        detalle1.precioConImpuestos = true;
        detalle1.icbAplica = true;

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(118);
        detalle2.precioConImpuestos = true;
        detalle2.icbAplica = true;

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "icb.xml");
        assertSendSunat(xml);
    }

    @Test
    public void testInvoiceWithCustomProveedor_direccionNotNullAndCodigoLocalNull() throws Exception {
        // Given
        BoletaFactura input = new BoletaFactura();
        input.serie = "F001";
        input.numero = 1;

        input.proveedor = new Proveedor();
        input.proveedor.ruc = "12345678912";
        input.proveedor.razonSocial = "Softgreen S.A.C.";
        input.proveedor.direccion = new Direccion();
        input.proveedor.direccion.direccion = "Jr. las flores 123";

        input.cliente = new Cliente();
        input.cliente.nombre = "Carlos Feria";
        input.cliente.numeroDocumentoIdentidad = "12121212121";
        input.cliente.tipoDocumentoIdentidad = Catalog6.RUC.toString();

        DocumentoDetalle detalle1 = new DocumentoDetalle();
        detalle1.descripcion = "Item1";
        detalle1.cantidad = new BigDecimal(10);
        detalle1.precio = new BigDecimal(118);

        DocumentoDetalle detalle2 = new DocumentoDetalle();
        detalle2.descripcion = "Item2";
        detalle2.cantidad = new BigDecimal(10);
        detalle2.precio = new BigDecimal(118);

        input.detalle = Arrays.asList(detalle1, detalle2);

        // When
        UniAssertSubscriber<String> subscriber = xmlBuilder.enrichAndRenderAsync(input).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();

        String xml = subscriber.getItem();
        assertSnapshot(xml, getClass(), "customCodigoLocal.xml");
        assertSendSunat(xml);
    }

}