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

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressLoadBalancerIngress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.github.project.openubl.operator.Constants;
import io.github.project.openubl.operator.utils.CRDUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.quarkus.logging.Log;

import java.util.Map;
import java.util.Optional;

public class UblhubIngress extends CRUDKubernetesDependentResource<Ingress, Ublhub> implements Condition<Ingress, Ublhub> {

    public UblhubIngress() {
        super(Ingress.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Ingress desired(Ublhub cr, Context<Ublhub> context) {
        boolean isIngressEnabled = cr.getSpec().getIngressSpec() != null && CRDUtils.getValueFromSubSpec(cr.getSpec().getIngressSpec(), UblhubSpec.IngressSpec::isEnabled)
                .orElse(false);

        return newIngress(cr, context);
    }

    @Override
    public boolean isMet(DependentResource<Ingress, Ublhub> dependentResource, Ublhub cr, Context<Ublhub> context) {
        boolean isIngressEnabled = CRDUtils.getValueFromSubSpec(cr.getSpec().getIngressSpec(), UblhubSpec.IngressSpec::isEnabled)
                .orElse(false);

        if (!isIngressEnabled) {
            return true;
        }

        return context.getSecondaryResource(Ingress.class)
                .map(in -> {
                    final var status = in.getStatus();
                    if (status != null) {
                        final var ingresses = status.getLoadBalancer().getIngress();
                        // only set the status if the ingress is ready to provide the info we need
                        return ingresses != null && !ingresses.isEmpty();
                    }
                    return false;
                })
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private Ingress newIngress(Ublhub cr, Context<Ublhub> context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        var port = UblhubService.getServicePort(cr);
        var backendProtocol = (!UblhubService.isTlsConfigured(cr)) ? "HTTP" : "HTTPS";

        Ingress ingress = new IngressBuilder()
                .withNewMetadata()
                .withName(cr.getMetadata().getName() + Constants.INGRESS_SUFFIX)
                .withNamespace(cr.getMetadata().getNamespace())
                .addToAnnotations("nginx.ingress.kubernetes.io/backend-protocol", backendProtocol)
                .addToAnnotations("route.openshift.io/termination", "passthrough")
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withNewDefaultBackend()
                .withNewService()
                .withName(cr.getMetadata().getName() + Constants.SERVICE_SUFFIX)
                .withNewPort()
                .withNumber(port)
                .endPort()
                .endService()
                .endDefaultBackend()
                .addNewRule()
                .withNewHttp()
                .addNewPath()
                .withPath("")
                .withPathType("ImplementationSpecific")
                .withNewBackend()
                .withNewService()
                .withName(cr.getMetadata().getName() + Constants.SERVICE_SUFFIX)
                .withNewPort()
                .withNumber(port)
                .endPort()
                .endService()
                .endBackend()
                .endPath()
                .endHttp()
                .endRule()
                .endSpec()
                .build();

        final var hostnameSpec = cr.getSpec().getHostnameSpec();
        if (hostnameSpec != null && hostnameSpec.getHostname() != null) {
            ingress.getSpec().getRules().get(0).setHost(hostnameSpec.getHostname());
        }
//        else {
//            getClusterDomainOnOpenshift(context).ifPresent(hostname -> {
//                ingress.getSpec().getRules().get(0).setHost(hostname);
//            });
//        }

        return ingress;
    }

    @SuppressWarnings("unchecked")
    private Optional<String> getClusterDomainOnOpenshift(Context<Ublhub> context) {
        final var k8sClient = (KubernetesClient) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_K8S_CLIENT_KEY, KubernetesClient.class);

        String clusterDomain = null;
        try {
            CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                    .withName("Ingress")
                    .withGroup("config.openshift.io")
                    .withVersion("v1")
                    .withPlural("ingresses")
                    .withScope("Cluster")
                    .build();
            GenericKubernetesResource clusterObject = k8sClient.genericKubernetesResources(customResourceDefinitionContext)
                    .withName("cluster")
                    .get();
            Map<String, String> objectSpec = clusterObject.get("spec");
            clusterDomain = objectSpec.get("domain");

            Log.info("Domain " + clusterDomain);
        } catch (KubernetesClientException exception) {
            // Nothing to do
            Log.info("No Openshift host found");
        }

        return Optional.ofNullable(clusterDomain);
    }

    public static Optional<String> getExposedURL(Ublhub cr, Ingress ingress) {
        final var status = ingress.getStatus();
        final var ingresses = status.getLoadBalancer().getIngress();
        Optional<IngressLoadBalancerIngress> ing = ingresses.isEmpty() ? Optional.empty() : Optional.of(ingresses.get(0));

        final var protocol = UblhubService.isTlsConfigured(cr) ? "https" : "http";
        return ing.map(i -> protocol + "://" + (i.getHostname() != null ? i.getHostname() : i.getIp()));
    }
}
