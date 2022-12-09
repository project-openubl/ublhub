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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.HTTPGetActionBuilder;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.github.project.openubl.operator.Config;
import io.github.project.openubl.operator.Constants;
import io.github.project.openubl.operator.ValueOrSecret;
import io.github.project.openubl.operator.controllers.UblhubDistConfigurator;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UblhubDeployment extends CRUDKubernetesDependentResource<Deployment, Ublhub> implements Matcher<Deployment, Ublhub> {

    public UblhubDeployment() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(Ublhub cr, Context<Ublhub> context) {
        UblhubDistConfigurator distConfigurator = new UblhubDistConfigurator(cr);
        return newDeployment(cr, context, distConfigurator);
    }

    @Override
    public Result<Deployment> match(Deployment actual, Ublhub cr, Context<Ublhub> context) {
        final var desiredSpec = cr.getSpec();
        final var container = actual.getSpec()
                .getTemplate().getSpec().getContainers()
                .stream()
                .findFirst();

        return Result.nonComputed(container
                .map(c -> c.getImage().equals(desiredSpec.getImage()))
                .orElse(false)
        );
    }

    @SuppressWarnings("unchecked")
    private Deployment newDeployment(Ublhub cr, Context<Ublhub> context, UblhubDistConfigurator distConfigurator) {
        final var contextLabels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(cr.getMetadata().getName() + Constants.DEPLOYMENT_SUFFIX)
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(contextLabels)
                .endMetadata()
                .withSpec(getDeploymentSpec(cr, context, distConfigurator))
                .build();
        return deployment;
    }

    @SuppressWarnings("unchecked")
    private DeploymentSpec getDeploymentSpec(Ublhub cr, Context<Ublhub> context, UblhubDistConfigurator distConfigurator) {
        final var config = (Config) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_CONFIG_KEY, Config.class);
        final var contextLabels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        Map<String, String> selectorLabels = Constants.DEFAULT_LABELS;
        String image = Optional.ofNullable(cr.getSpec().getImage()).orElse(config.ublhub().image());
        String imagePullPolicy = config.ublhub().imagePullPolicy();

        List<EnvVar> envVars = Stream.concat(
                getEnvVars(cr, config).stream(),
                distConfigurator.getAllEnvVars().stream()
        ).collect(Collectors.toList());
        List<Volume> volumes = distConfigurator.getAllVolumes();
        List<VolumeMount> volumeMounts = distConfigurator.getAllVolumeMounts();

        var tlsConfigured = UblhubService.isTlsConfigured(cr);
        var protocol = !tlsConfigured ? "http" : "https";
        var port = UblhubService.getServicePort(cr);

        var baseProbe = new ArrayList<>(List.of("curl", "--head", "--fail", "--silent"));
        if (tlsConfigured) {
            baseProbe.add("--insecure");
        }

        return new DeploymentSpecBuilder()
                .withReplicas(cr.getSpec().getInstances())
                .withSelector(new LabelSelectorBuilder()
                        .withMatchLabels(selectorLabels)
                        .build()
                )
                .withTemplate(new PodTemplateSpecBuilder()
                        .withNewMetadata()
                        .withLabels(Stream
                                .concat(contextLabels.entrySet().stream(), selectorLabels.entrySet().stream())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        )
                        .endMetadata()
                        .withSpec(new PodSpecBuilder()
                                .withRestartPolicy("Always")
                                .withTerminationGracePeriodSeconds(30L)
                                .withImagePullSecrets(cr.getSpec().getImagePullSecrets())
                                .withContainers(new ContainerBuilder()
                                        .withName(Constants.UBLHUB_NAME)
                                        .withImage(image)
                                        .withImagePullPolicy(imagePullPolicy)
                                        .withEnv(envVars)
                                        .withPorts(
                                                new ContainerPortBuilder()
                                                        .withName("http")
                                                        .withProtocol("TCP")
                                                        .withContainerPort(8080)
                                                        .build(),
                                                new ContainerPortBuilder()
                                                        .withName("https")
                                                        .withProtocol("TCP")
                                                        .withContainerPort(8443)
                                                        .build()
                                        )
                                        .withReadinessProbe(new ProbeBuilder()
                                                .withHttpGet(new HTTPGetActionBuilder()
                                                        .withPath("/q/health/ready")
                                                        .withNewPort("http")
                                                        .build()
                                                )
                                                .withInitialDelaySeconds(20)
                                                .withPeriodSeconds(2)
                                                .withFailureThreshold(250)
                                                .build()
                                        )
                                        .withLivenessProbe(new ProbeBuilder()
                                                .withHttpGet(new HTTPGetActionBuilder()
                                                        .withPath("/q/health/live")
                                                        .withNewPort("http")
                                                        .build()
                                                )
                                                .withInitialDelaySeconds(20)
                                                .withPeriodSeconds(2)
                                                .withFailureThreshold(150)
                                                .build()
                                        )
                                        .withVolumeMounts(volumeMounts)
                                        .build()
                                )
                                .withVolumes(volumes)
                                .build()
                        )
                        .build()
                )
                .build();
    }

    private List<EnvVar> getEnvVars(Ublhub cr, Config config) {
        // default config values
        List<ValueOrSecret> serverConfig = Constants.DEFAULT_DIST_CONFIG.entrySet().stream()
                .map(e -> new ValueOrSecret(e.getKey(), e.getValue(), null))
                .collect(Collectors.toList());

        // merge with the CR; the values in CR take precedence
        if (cr.getSpec().getAdditionalOptions() != null) {
            serverConfig.removeAll(cr.getSpec().getAdditionalOptions());
            serverConfig.addAll(cr.getSpec().getAdditionalOptions());
        }

        // set env vars
        List<EnvVar> envVars = serverConfig.stream()
                .map(v -> {
                    String envValue = v.getValue();
                    EnvVarSource envValueFrom = new EnvVarSourceBuilder()
                            .withSecretKeyRef(v.getSecret())
                            .build();

                    return new EnvVarBuilder()
                            .withName(v.getName())
                            .withValue(v.getSecret() == null ? envValue : null)
                            .withValueFrom(v.getSecret() != null ? envValueFrom : null)
                            .build();
                })
                .collect(Collectors.toList());

        return envVars;
    }
}
