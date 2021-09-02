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
package io.github.project.openubl.xsender;

import io.github.project.openubl.xsender.containers.MinioServer;
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
