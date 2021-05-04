package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.SunatCredentialsRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.*;
import io.github.project.openubl.xsender.resources.config.BaseKeycloakTest;
import io.github.project.openubl.xsender.resources.config.KafkaServer;
import io.github.project.openubl.xsender.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.resources.config.PostgreSQLServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(KafkaServer.class)
public class CompanyResourceTest extends BaseKeycloakTest {

    final SunatCredentialsEntity credentials = SunatCredentialsEntity.Builder.aSunatCredentialsEntity()
            .withSunatUsername("anyUsername")
            .withSunatPassword("anyPassword")
            .build();
    final SunatUrlsEntity urls = SunatUrlsEntity.Builder.aSunatUrlsEntity()
            .withSunatUrlFactura("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl")
            .withSunatUrlGuiaRemision("https://e-guiaremision.sunat.gob.pe/ol-ti-itemision-guia-gem/billService?wsdl")
            .withSunatUrlPercepcionRetencion("https://e-factura.sunat.gob.pe/ol-ti-itemision-otroscpe-gem/billService?wsdl")
            .build();

    NamespaceEntity namespace;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    CompanyRepository companyRepository;

    @BeforeEach
    public void beforeEach() {
        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("alice")
                .withCreatedOn(new Date())
                .build();

        this.companyRepository.deleteAll();
        this.namespaceRepository.deleteAll();

        this.namespaceRepository.persist(namespace);
        this.namespace = namespace;
    }

    @Test
    public void getCompany() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/companies/" + company.getId())
                .then()
                .statusCode(200)
                .body("name", is(company.getName()),
                        "ruc", is(company.getRuc())
                );

        // Then
    }

    @Test
    public void getCompanyByNotOwner_shouldNotBeAllowed() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/companies/" + company.getId())
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/companies/" + company.getId())
                .then()
                .statusCode(200);
        // Then
    }

    @Test
    public void updateCompany() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        CompanyRepresentation companyRepresentation = CompanyRepresentation.CompanyRepresentationBuilder.aCompanyRepresentation()
                .withRuc("11111111111")
                .withName("new name")
                .withDescription("new description")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://newUrl1.com")
                        .withRetenciones("http://newUrl2.com")
                        .withGuia("http://newUrl3.com")
                        .build()
                )
                .withCredentials(SunatCredentialsRepresentation.Builder.aSunatCredentialsRepresentation()
                        .withUsername("new username")
                        .withPassword("new password")
                        .build()
                )
                .build();

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/api/companies/" + company.getId())
                .then()
                .statusCode(200)
                .body("ruc", is(companyRepresentation.getRuc()),
                        "name", is(companyRepresentation.getName()),
                        "description", is(companyRepresentation.getDescription()),
                        "webServices.factura", is(companyRepresentation.getWebServices().getFactura()),
                        "webServices.retenciones", is(companyRepresentation.getWebServices().getRetenciones()),
                        "webServices.guia", is(companyRepresentation.getWebServices().getGuia()),
                        "credentials.username", is(companyRepresentation.getCredentials().getUsername()),
                        "credentials.password", is(nullValue())
                );

        // Then
        assertEquals(companyRepresentation.getCredentials().getPassword(), companyRepository.findById(company.getId()).getSunatCredentials().getSunatPassword());

        OutboxEventEntity kafkaMsg = OutboxEventEntity.findByParams(EntityType.company.toString(), company.getId(), EventType.UPDATED.toString());
        assertNotNull(kafkaMsg);
    }

    @Test
    public void updateCompanyByNotOwner_shouldNotBeAllowed() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        CompanyRepresentation companyRepresentation = CompanyRepresentation.CompanyRepresentationBuilder.aCompanyRepresentation()
                .withRuc("11111111111")
                .withName("new name")
                .withDescription("new description")
                .withWebServices(SunatUrlsRepresentation.Builder.aSunatUrlsRepresentation()
                        .withFactura("http://newUrl1.com")
                        .withRetenciones("http://newUrl2.com")
                        .withGuia("http://newUrl3.com")
                        .build()
                )
                .build();

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(companyRepresentation)
                .when()
                .put("/api/companies/" + company.getId())
                .then()
                .statusCode(404);

        // Then

    }

    @Test
    public void deleteCompany() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/companies/" + company.getId())
                .then()
                .statusCode(204);

        // Then
        assertNotNull(companyRepository.findById(company.getId()));
    }

    @Test
    public void deleteCompany_byNotOwner() {
        // Given
        CompanyEntity company = CompanyEntity.CompanyEntityBuilder.aCompanyEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my company")
                .withRuc("12345678910")
                .withCreatedOn(new Date())
                .withSunatCredentials(credentials)
                .withSunatUrls(urls)
                .withNamespace(namespace)
                .build();

        companyRepository.persist(company);

        // When
        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/companies/" + company.getId())
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/companies/" + company.getId())
                .then()
                .statusCode(204);

        // Then
    }

}
