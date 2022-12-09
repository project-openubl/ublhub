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
package io.github.project.openubl.operator.cdrs.v2alpha1;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.github.project.openubl.operator.Constants;
import io.github.project.openubl.operator.utils.CRDUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.Map;

public class UblhubService extends CRUDKubernetesDependentResource<Service, Ublhub> {

    public UblhubService() {
        super(Service.class);
    }

    @Override
    public Service desired(Ublhub cr, Context context) {
        return newService(cr, context);
    }

    @SuppressWarnings("unchecked")
    private Service newService(Ublhub cr, Context context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(cr.getMetadata().getName() + Constants.SERVICE_SUFFIX)
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .endMetadata()
                .withSpec(getServiceSpec(cr))
                .build();
        return service;
    }

    private ServiceSpec getServiceSpec(Ublhub cr) {
        return new ServiceSpecBuilder()
                .addNewPort()
                .withPort(getServicePort(cr))
                .withProtocol(Constants.SERVICE_PROTOCOL)
                .endPort()
                .withSelector(Constants.DEFAULT_LABELS)
                .withType("ClusterIP")
                .build();
    }

    public static int getServicePort(Ublhub cr) {
        // we assume HTTP when TLS is not configured
        if (!isTlsConfigured(cr)) {
            return Constants.HTTP_PORT;
        } else {
            return Constants.HTTPS_PORT;
        }
    }

    public static boolean isTlsConfigured(Ublhub cr) {
        var tlsSecret = CRDUtils.getValueFromSubSpec(cr.getSpec().getHttpSpec(), UblhubSpec.HttpSpec::getTlsSecret);
        return tlsSecret.isPresent() && !tlsSecret.get().trim().isEmpty();
    }

}
