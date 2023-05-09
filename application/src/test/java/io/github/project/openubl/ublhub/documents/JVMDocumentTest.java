package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.ublhub.resources.DocumentResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import java.util.Map;

@QuarkusTest
@TestProfile(JVMDocumentTest.Profile.class)
@TestHTTPEndpoint(DocumentResource.class)
public class JVMDocumentTest extends AbstractDocumentTest {

    public static class Profile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("openubl.scheduler.type", "jvm");
        }
    }
}
