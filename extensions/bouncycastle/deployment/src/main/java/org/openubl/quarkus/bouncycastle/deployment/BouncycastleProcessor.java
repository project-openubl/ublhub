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
package org.openubl.quarkus.bouncycastle.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

class BouncycastleProcessor {

    private static final String FEATURE = "bouncycastle";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void reflective(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(
                new ReflectiveClassBuildItem(
                        false,
                        false,
                        "org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi",
                        "org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory"));
    }

    @BuildStep
    void configure(BuildProducer<RuntimeInitializedClassBuildItem> runtimeClasses) {
        runtimeClasses.produce(new RuntimeInitializedClassBuildItem(org.bouncycastle.crypto.prng.SP800SecureRandom.class.getCanonicalName()));
        runtimeClasses.produce(new RuntimeInitializedClassBuildItem("org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV"));
        runtimeClasses.produce(new RuntimeInitializedClassBuildItem("org.bouncycastle.jcajce.provider.drbg.DRBG$Default"));
    }
}
