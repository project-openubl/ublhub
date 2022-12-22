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

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.github.project.openubl.operator.Constants;
import io.github.project.openubl.operator.utils.CRDUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.Map;

public class UblhubFileStoragePVC extends CRUDKubernetesDependentResource<PersistentVolumeClaim, Ublhub> {

    public UblhubFileStoragePVC() {
        super(PersistentVolumeClaim.class);
    }

    @Override
    public PersistentVolumeClaim desired(Ublhub cr, Context context) {
        return newPersistentVolumeClaim(cr, context);
    }

    @SuppressWarnings("unchecked")
    private PersistentVolumeClaim newPersistentVolumeClaim(Ublhub cr, Context context) {
        final var labels = (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(Constants.CONTEXT_LABELS_KEY, Map.class);

        PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName(getPersistentVolumeClaimName(cr))
                .withNamespace(cr.getMetadata().getNamespace())
                .withLabels(labels)
                .endMetadata()
                .withSpec(getPersistentVolumeClaimSpec(cr))
                .build();
        return pvc;
    }

    private PersistentVolumeClaimSpec getPersistentVolumeClaimSpec(Ublhub cr) {
        boolean isFileSystemStorage = cr.getSpec().getStorageSpec() != null && cr.getSpec().getStorageSpec().getType().equals(UblhubSpec.StorageSpec.Type.filesystem);
        Quantity storageSize = isFileSystemStorage ?
                new Quantity(cr.getSpec().getStorageSpec().getFilesystemSpec().getSize()) :
                new Quantity(Constants.STORAGE_MIN_SIZE);

        return new PersistentVolumeClaimSpecBuilder()
                .withAccessModes("ReadWriteOnce")
                .withResources(new ResourceRequirementsBuilder()
                        .addToRequests("storage", storageSize)
                        .build()
                )
                .build();
    }

    public static String getPersistentVolumeClaimName(Ublhub cr) {
        return cr.getMetadata().getName() + "-filesystem" + Constants.PVC_SUFFIX;
    }

    public static boolean isTlsConfigured(Ublhub cr) {
        var tlsSecret = CRDUtils.getValueFromSubSpec(cr.getSpec().getHttpSpec(), UblhubSpec.HttpSpec::getTlsSecret);
        return tlsSecret.isPresent() && !tlsSecret.get().trim().isEmpty();
    }

}
