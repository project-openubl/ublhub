package io.github.project.openubl.xsender;

import io.github.project.openubl.xsender.resources.config.MinioServer;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager implements QuarkusTestProfile {

    static final String testModeKey = "xsender.test.mode";

    enum DistributionFlavor {
        standalone, enterprise
    }

    String configProfile;
    List<TestResourceEntry> testResources = new ArrayList<>();

    public ProfileManager() {
        String testModeFlavor = System.getProperty(testModeKey, DistributionFlavor.standalone.toString());
        DistributionFlavor distributionFlavor = DistributionFlavor.valueOf(testModeFlavor);

        switch (distributionFlavor) {
            case standalone:
                // Profile
                configProfile = "test";

                // Test resources
                break;
            case enterprise:
                // Profile
                configProfile = DistributionFlavor.enterprise.toString();

                // Test resources
                testResources.add(new TestResourceEntry(MinioServer.class));
                break;
        }
    }

    @Override
    public String getConfigProfile() {
        return configProfile;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return testResources;
    }
}
