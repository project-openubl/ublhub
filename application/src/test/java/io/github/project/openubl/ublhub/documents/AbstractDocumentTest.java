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
package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.ublhub.AbstractBaseTest;
import io.github.project.openubl.ublhub.ResourceHelpers;
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.dto.ProjectDto;
import io.github.project.openubl.ublhub.dto.SunatDto;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.content.models.common.Cliente;
import io.github.project.openubl.xbuilder.content.models.common.Proveedor;
import io.github.project.openubl.xbuilder.content.models.standard.general.DocumentoVentaDetalle;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;

public abstract class AbstractDocumentTest extends AbstractBaseTest {

    @Inject
    ResourceHelpers resourceHelpers;

    final int TIMEOUT = 60;

    final static SunatDto sunatDto = SunatDto.builder()
            .facturaUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
            .guiaUrl("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
            .retencionUrl("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem")
            .username("MODDATOS")
            .password("MODDATOS")
            .build();

    final static Invoice invoice = Invoice.builder()
            .serie("F001")
            .numero(1)
            .proveedor(Proveedor.builder()
                    .ruc("12345678912")
                    .razonSocial("Softgreen S.A.C.")
                    .build()
            )
            .cliente(Cliente.builder()
                    .nombre("Carlos Feria")
                    .numeroDocumentoIdentidad("12121212121")
                    .tipoDocumentoIdentidad(Catalog6.RUC.toString())
                    .build()
            )
            .detalle(DocumentoVentaDetalle.builder()
                    .descripcion("Item1")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .detalle(DocumentoVentaDetalle.builder()
                    .descripcion("Item2")
                    .cantidad(new BigDecimal(10))
                    .precio(new BigDecimal(100))
                    .build()
            )
            .build();

    @BeforeEach
    public void beforeEach() {
        cleanDB();
        resourceHelpers.generatePreexistingData();
    }
    @Test
    public void createInvoiceWithDefaultSignAlgorithm() {
        // Given
        String project = "myproject";

        ProjectDto projectDto = ProjectDto.builder()
                .name(project)
                .description("my description")
                .sunat(sunatDto)
                .build();

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(projectDto)
                .when()
                .post("/")
                .then()
                .statusCode(201);

        // When
        JsonObject inputDto = Json.createObjectBuilder()
                .add("kind", DocumentInputDto.Kind.Invoice.toString())
                .add("spec", Json.createObjectBuilder()
                        .add("document", toJavax(invoice))
                        .build()
                )
                .build();

        // Then
        String documentId = givenAuth("alice")
                .contentType(ContentType.JSON)
                .body(inputDto.toString())
                .when()
                .post("/" + project + "/documents")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()),
                        "status.inProgress", is(true)
                )
                .extract().path("id").toString();

        await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> {
            String inProgress = givenAuth("alice")
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/" + project + "/documents/" + documentId)
                    .then()
                    .statusCode(200)
                    .extract().path("status.inProgress").toString();
            return !Boolean.parseBoolean(inProgress);
        });

        givenAuth("alice")
                .contentType(ContentType.JSON)
                .when()
                .get("/" + project + "/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("status.inProgress", is(false),
                        "status.xmlData.ruc", is("12345678912"),
                        "status.xmlData.serieNumero", is("F001-1"),
                        "status.xmlData.tipoDocumento", is("Invoice"),
                        "status.error", is(nullValue()),
                        "status.sunat.code", is(0),
                        "status.sunat.ticket", is(nullValue()),
                        "status.sunat.status", is("ACEPTADO"),
                        "status.sunat.description", is("La Factura numero F001-1, ha sido aceptada"),
                        "status.sunat.hasCdr", is(true),
                        "status.sunat.notes", is(Collections.emptyList())
                );
    }

}

