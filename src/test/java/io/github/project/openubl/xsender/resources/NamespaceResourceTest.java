package io.github.project.openubl.xsender.resources;

import io.github.project.openubl.xsender.kafka.producers.EntityType;
import io.github.project.openubl.xsender.kafka.producers.EventType;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.OutboxEventEntity;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
@QuarkusTestResource(KafkaServer.class)
public class NamespaceResourceTest extends BaseKeycloakTest {

    @Inject
    NamespaceRepository namespaceRepository;

    @BeforeEach
    public void beforeEach() {
        namespaceRepository.deleteAll();
    }

    @Test
    public void getNamespace() {
        // Given

        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("alice")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + namespace.getName())
                .then()
                .statusCode(200)
                .body("name", is(namespace.getName()),
                        "description", is(namespace.getDescription())
                );

        // Then

    }

    @Test
    public void getNamespaceByNotOwner_shouldReturnNotFound() {
        // Given

        NamespaceEntity namespace1 = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace1")
                .withOwner("admin")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace1);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/namespaces/" + namespace1.getName())
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .header("Content-Type", "application/json")
                .when()
                .get("/api/namespaces/" + namespace1.getName())
                .then()
                .statusCode(200);

        // Then
    }

    @Test
    public void updateNamespace() {
        // Given
        String NAME = "my-namespace";

        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName(NAME)
                .withOwner("admin")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace);

        // When
        NamespaceEntity namespaceUpdate = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withDescription("my description")
                .build();

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespaceUpdate)
                .when()
                .put("/api/namespaces/" + NAME)
                .then()
                .statusCode(200)
                .body("name", is(NAME),
                        "description", is(namespaceUpdate.getDescription())
                );

        // Then
        OutboxEventEntity kafkaMsg = OutboxEventEntity.findByParams(EntityType.namespace.toString(), namespace.getId(), EventType.UPDATED.toString());
        assertNotNull(kafkaMsg);
    }

    @Test
    public void updateNamespaceByNotOwner_shouldNotBeAllowed() {
        // Given
        String NAME = "my-namespace";

        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName(NAME)
                .withOwner("admin")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace);

        // When
        NamespaceEntity namespaceUpdate = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withDescription("my description")
                .build();

        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespaceUpdate)
                .when()
                .put("/api/namespaces/" + NAME)
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(namespaceUpdate)
                .when()
                .put("/api/namespaces/" + NAME)
                .then()
                .statusCode(200);

        // Then
    }

    @Test
    public void deleteNamespace() {
        // Given
        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("alice")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/namespaces/" + namespace.getName())
                .then()
                .statusCode(204);

        // Then
        OutboxEventEntity kafkaMsg = OutboxEventEntity.findByParams(EntityType.namespace.toString(), namespace.getId(), EventType.DELETED.toString());
        assertNotNull(kafkaMsg);
    }

    @Test
    public void deleteNamespaceByNotOwner_shouldNotBeAllowed() {
        // Given
        NamespaceEntity namespace = NamespaceEntity.NamespaceEntityBuilder.aNamespaceEntity()
                .withId(UUID.randomUUID().toString())
                .withName("my-namespace")
                .withOwner("admin")
                .withCreatedOn(new Date())
                .build();
        namespaceRepository.persist(namespace);

        // When
        given().auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/namespaces/" + namespace.getName())
                .then()
                .statusCode(404);

        given().auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/namespaces/" + namespace.getName())
                .then()
                .statusCode(204);

        // Then

    }

}
