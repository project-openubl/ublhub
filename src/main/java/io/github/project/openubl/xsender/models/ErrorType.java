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
package io.github.project.openubl.xsender.models;

public enum ErrorType {
    FETCH_FILE("No se pudo recuperar el archivo"),
    READ_FILE("Documento no pudo ser parseado"),
    UNSUPPORTED_DOCUMENT_TYPE("Tipo de documento no válido"),
    COMPANY_NOT_FOUND("No se pudo encontrar una empresa para el archivo"),
    SEND_FILE("No se pudo enviar el archivo a la SUNAT"),
    SAVE_CRD_FILE("No se pudo guardar el CDR"),
    AMQP_SCHEDULE("No se pudo programar envio"),
    RETRY_CONSUMED("Reenvios agotados"),
    UNKNOWN("Reenvios agotados"),
    ;

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
