/**
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
package io.github.project.openubl.xsender.basic.resources;

import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
public class CorporateResourceTest {

//    @Test
//    public void createCorporate() throws JsonProcessingException {
//        final String CORPORATE_NAME = "myName";
//
//        // GIVEN
//        OrganizationRepresentation corporateRep = OrganizationRepresentation.Builder.aCorporateRepresentation()
//                .withName(CORPORATE_NAME)
//                .withDescription("myDescription")
//                .withSunatUrls(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
//                        .withFactura("myFacturaUrl")
//                        .withGuiaRemision("myGuiaUrl")
//                        .withPercepcionRetencion("myPerceptionRetentionUrl")
//                        .build()
//                )
//                .build();
//
//        String body = new ObjectMapper().writeValueAsString(corporateRep);
//
//        // WHEN
//        given()
//                .body(body)
//                .header("Content-Type", "application/json")
//                .when()
//                .post("/" + OrganizationsResource.CORPORATE_PATH)
//                .then()
//                .body("name", is(corporateRep.getName()),
//                        "description", is(corporateRep.getDescription()),
//                        "sunatUrls.factura", is(corporateRep.getSunatUrls().getFactura()),
//                        "sunatUrls.guiaRemision", is(corporateRep.getSunatUrls().getGuiaRemision()),
//                        "sunatUrls.percepcionRetencion", is(corporateRep.getSunatUrls().getPercepcionRetencion())
//                );
//
//        given()
//                .header("Content-Type", "application/json")
//                .when()
//                .get("/" + OrganizationsResource.CORPORATE_PATH + "/" + CORPORATE_NAME)
//                .then()
//                .body("name", is(corporateRep.getName()),
//                        "description", is(corporateRep.getDescription()),
//                        "sunatUrls.factura", is(corporateRep.getSunatUrls().getFactura()),
//                        "sunatUrls.guiaRemision", is(corporateRep.getSunatUrls().getGuiaRemision()),
//                        "sunatUrls.percepcionRetencion", is(corporateRep.getSunatUrls().getPercepcionRetencion())
//                );
//    }
//
//    @Test
//    public void updateCorporate() throws JsonProcessingException {
//        final String CORPORATE_NAME = "myNameToUpdate";
//
//        // GIVEN
//        OrganizationRepresentation corporateRep = OrganizationRepresentation.Builder.aCorporateRepresentation()
//                .withName(CORPORATE_NAME)
//                .withDescription("myDescription")
//                .build();
//        String body = new ObjectMapper().writeValueAsString(corporateRep);
//
//        given()
//                .body(body)
//                .header("Content-Type", "application/json")
//                .when()
//                .post("/" + OrganizationsResource.CORPORATE_PATH)
//                .then()
//                .body("name", is(corporateRep.getName()),
//                        "description", is(corporateRep.getDescription()),
//                        "sunatUrls", is(nullValue())
//                );
//
//        // WHEN
//        corporateRep = OrganizationRepresentation.Builder.aCorporateRepresentation()
//                .withName("nameChanged")
//                .withDescription("descriptionChanged")
//                .withSunatUrls(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
//                        .withFactura("myFacturaUrl")
//                        .withGuiaRemision("myGuiaUrl")
//                        .withPercepcionRetencion("myPerceptionRetentionUrl")
//                        .build()
//                )
//                .build();
//        body = new ObjectMapper().writeValueAsString(corporateRep);
//
//        given()
//                .body(body)
//                .header("Content-Type", "application/json")
//                .when()
//                .put("/" + OrganizationsResource.CORPORATE_PATH + "/" + CORPORATE_NAME)
//                .then()
//                .body("name", is(CORPORATE_NAME), // name shouldn't change
//                        "description", is(corporateRep.getDescription()),
//                        "sunatUrls.factura", is(corporateRep.getSunatUrls().getFactura()),
//                        "sunatUrls.guiaRemision", is(corporateRep.getSunatUrls().getGuiaRemision()),
//                        "sunatUrls.percepcionRetencion", is(corporateRep.getSunatUrls().getPercepcionRetencion())
//                );
//    }
//
//    @Test
//    public void updateCorporateCredentials() throws JsonProcessingException {
//        final String CORPORATE_NAME = "myName" + UUID.randomUUID().toString();
//
//        // GIVEN
//        OrganizationRepresentation corporateRep = OrganizationRepresentation.Builder.aCorporateRepresentation()
//                .withName(CORPORATE_NAME)
//                .build();
//        String body = new ObjectMapper().writeValueAsString(corporateRep);
//
//        given()
//                .body(body)
//                .header("Content-Type", "application/json")
//                .when()
//                .post("/" + OrganizationsResource.CORPORATE_PATH)
//                .then()
//                .body("name", is(corporateRep.getName()));
//
//        // WHEN
//        SunatCredentialsRepresentation credentialsRep = SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
//                .withSunatUsername("myUsername")
//                .withSunatPassword("myPassword")
//                .build();
//        body = new ObjectMapper().writeValueAsString(credentialsRep);
//
//        given()
//                .body(body)
//                .header("Content-Type", "application/json")
//                .when()
//                .put("/" + OrganizationsResource.CORPORATE_PATH + "/" + CORPORATE_NAME + "/sunat-credentials")
//                .then()
//                .statusCode(204);
//    }

}
