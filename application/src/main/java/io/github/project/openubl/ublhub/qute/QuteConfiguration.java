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
package io.github.project.openubl.ublhub.qute;

import io.github.project.openubl.xbuilder.content.catalogs.*;
import io.github.project.openubl.xbuilder.content.models.common.Direccion;
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.RawString;
import io.quarkus.qute.ValueResolver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class QuteConfiguration {

    void configureEngine(@Observes EngineBuilder builder) {
        builder
                .addValueResolver(ValueResolver.builder()
                        .applyToBaseClass(String.class)
                        .applyToName("invoiceType")
                        .resolveSync(ctx -> {
                            String serie = (String) ctx.getBase();
                            return serie.toUpperCase().startsWith("F") ? "FACTURA" : "BOLETA";
                        })
                        .build()
                )
                .addValueResolver(ValueResolver.builder()
                        .applyToBaseClass(Direccion.class)
                        .applyToName("print")
                        .resolveSync(ctx -> {
                            Direccion direccion = (Direccion) ctx.getBase();

                            // Address
                            String address = direccion.getDireccion();

                            // Zip code
                            String depProvDist = Stream.of(direccion.getDepartamento(), direccion.getProvincia(), direccion.getDistrito())
                                    .filter(s -> s != null && !s.trim().isEmpty())
                                    .collect(Collectors.joining("/"));

                            String zipCode = Stream.of(depProvDist, direccion.getUbigeo())
                                    .filter(s -> s != null && !s.trim().isEmpty())
                                    .collect(Collectors.joining(", "));

                            // Sub Zip code
                            String subZipCode = Stream.of(direccion.getUrbanizacion(), direccion.getCodigoLocal())
                                    .filter(s -> s != null && !s.trim().isEmpty())
                                    .collect(Collectors.joining("-"));

                            // Country
                            String countryCode = direccion.getCodigoPais();

                            String result = Stream.of(
                                            address,
                                            zipCode,
                                            subZipCode,
                                            countryCode
                                    )
                                    .filter(s -> s != null && !s.trim().isEmpty())
                                    .collect(Collectors.joining("<br>"));
                            return new RawString(result);
                        })
                        .build()
                )
                .addValueResolver(ValueResolver.builder()
                        .applyToBaseClass(String.class)
                        .applyToName("catalog")
                        .resolveSync(ctx -> {
                            String catalogCode = (String) ctx.getBase();
                            int catalogNumber = (int) ctx.getParams().get(0).getLiteral();
                            return switch (catalogNumber) {
                                case 1 -> Catalog.valueOfCode(Catalog1.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 5 -> Catalog.valueOfCode(Catalog5.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 6 -> Catalog.valueOfCode(Catalog6.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 7 -> Catalog.valueOfCode(Catalog7.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 8 -> Catalog.valueOfCode(Catalog8.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 9 -> Catalog.valueOfCode(Catalog9.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 10 -> Catalog.valueOfCode(Catalog10.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 12 -> Catalog.valueOfCode(Catalog12.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 16 -> Catalog.valueOfCode(Catalog16.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 18 -> Catalog.valueOfCode(Catalog18.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 19 -> Catalog.valueOfCode(Catalog19.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 20 -> Catalog.valueOfCode(Catalog20.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 21 -> Catalog.valueOfCode(Catalog21.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 22 -> Catalog.valueOfCode(Catalog22.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 23 -> Catalog.valueOfCode(Catalog23.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 51 -> Catalog.valueOfCode(Catalog51.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 52 -> Catalog.valueOfCode(Catalog52.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 53 -> Catalog.valueOfCode(Catalog53.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 54 -> Catalog.valueOfCode(Catalog54.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                case 59 -> Catalog.valueOfCode(Catalog59.class, catalogCode)
                                        .map(Enum::toString)
                                        .orElse(null);
                                default -> throw new IllegalStateException("Unexpected value: " + catalogNumber);
                            };
                        })
                        .build()
                );
    }

}
