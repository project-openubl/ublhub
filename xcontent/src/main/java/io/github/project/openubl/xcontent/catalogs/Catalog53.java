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
package io.github.project.openubl.xcontent.catalogs;

public enum Catalog53 implements Catalog {

    OTROS_DESCUENTOS("00"),
    OTROS_CARGOS("50"),
    PERCEPCION_VENTA_INTERNA("51"),
    PERCEPCION_A_LA_ADQUISICION_DE_COMBUSTIBLE("52"),
    PERCEPCION_REALIZADA_AL_AGENTE__DE_PERCEPCION_CON_TASA_ESPECIAL("53"),
    OTROS_CARGOS_RELACIONADOS_AL_SERVICIO("54"),
    OTROS_CARGOS_NO_RELACIONADOS_AL_SERVICIO("55");

    private final String code;

    Catalog53(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
