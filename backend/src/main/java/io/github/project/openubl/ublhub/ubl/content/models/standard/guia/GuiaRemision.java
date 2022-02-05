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
package io.github.project.openubl.ublhub.ubl.content.models.standard.guia;

import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog1;
import io.github.project.openubl.ublhub.ubl.content.catalogs.Catalog21;
import io.github.project.openubl.ublhub.ubl.content.catalogs.validation.CatalogConstraint;
import io.github.project.openubl.ublhub.ubl.content.models.common.Cliente;
import io.github.project.openubl.ublhub.ubl.content.models.common.Firmante;
import io.github.project.openubl.ublhub.ubl.content.models.common.Proveedor;
import io.github.project.openubl.ublhub.ubl.content.models.standard.general.DespatchAdviceTraslado;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

public class GuiaRemision {

    @NotNull
    @NotBlank
    @Pattern(regexp = "^[T].*$")
    @Size(min = 4, max = 4)
    protected String serie;

    @Min(1)
    @NotNull
    public Integer numero;

    public Long fechaEmision;
    public String observacion;

    @Valid
    public GuiaRemision.GuiaRemisionBajaInputModel guiaRemisionDadaDeBaja;

    @Valid
    public GuiaRemision.DocumentoAdicionalRelacionadoInputModel documentoAdicionalRelacionado;

    @Valid
    public Firmante firmante;

    @Valid
    @NotNull
    public Proveedor remitente;

    @Valid
    @NotNull
    public Cliente destinatario;

    @Valid
    @NotNull
    public DespatchAdviceTraslado traslado;

    @Valid
    public Cliente transportista;

    @Valid
    public Cliente conductor;

    @Valid
    public GuiaRemision.VehiculoInputModel vehiculo;

    @Valid
    @NotNull
    @NotEmpty
    public List<GuiaRemisionDetalle> detalle;

    public static class GuiaRemisionBajaInputModel {
        @NotBlank
        public String serieNumero;

        @NotBlank
        @CatalogConstraint(value = Catalog1.class)
        public String tipoDocumento;

        public String getSerieNumero() {
            return serieNumero;
        }

        public void setSerieNumero(String serieNumero) {
            this.serieNumero = serieNumero;
        }

        public String getTipoDocumento() {
            return tipoDocumento;
        }

        public void setTipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
        }
    }

    public static class DocumentoAdicionalRelacionadoInputModel {
        @NotBlank
        public String serieNumero;

        @NotBlank
        @CatalogConstraint(value = Catalog21.class)
        public String tipoDocumento;

        public String getSerieNumero() {
            return serieNumero;
        }

        public void setSerieNumero(String serieNumero) {
            this.serieNumero = serieNumero;
        }

        public String getTipoDocumento() {
            return tipoDocumento;
        }

        public void setTipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
        }
    }

    public static class VehiculoInputModel {
        @NotBlank
        public String placa;

        public String getPlaca() {
            return placa;
        }

        public void setPlaca(String placa) {
            this.placa = placa;
        }
    }
}
